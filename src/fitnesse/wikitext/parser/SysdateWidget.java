// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import fitnesse.html.HtmlTag;
import fitnesse.util.Clock;

public class SysdateWidget extends SymbolType implements Rule, Translation
{

  public SysdateWidget()
  {
    super("Sysdate");
    wikiMatcher(new Matcher().string("!sysdate"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public String toTarget(final Translator translator, final Symbol symbol)
  {
    final HtmlTag result = new HtmlTag("span", "sysdate defined: " + translator.translate(symbol.childAt(0)) + "=" + translator.translate(symbol.childAt(1)));
    result.addAttribute("class", "meta");
    return result.html();
  }

  @Override
  public Maybe<Symbol> parse(final Symbol symbol, final Parser parser)
  {
    if (!parser.isMoveNext(SymbolType.Whitespace))
    {
      return Symbol.nothing;
    }

    final Maybe<String> name = parser.parseToAsString(SymbolType.Whitespace);

    if (parser.atEnd())
    {
      return Symbol.nothing;
    }

    if (!ScanString.isVariableName(name.getValue()))
    {
      return Symbol.nothing;
    }

    final Symbol next = parser.moveNext(1);
    final SymbolType close = next.closeType();
    if (close == SymbolType.Empty)
    {
      return Symbol.nothing;
    }

    final Maybe<String> valueString = parser.parseToAsString(close);

    String value = "";
    try
    {
      value = evaluateDateValue(valueString.getValue());
    }
    catch (Exception e)
    {
      return Symbol.nothing;
    }

    if (parser.atEnd())
    {
      return Symbol.nothing;
    }

    parser.getPage().putVariable(name.getValue(), value);

    return new Maybe<Symbol>(symbol.add(name.getValue()).add(value));
  }

  private String evaluateDateValue(final String param) throws ParseException
  {

    String result = "Invalid date format: " + param;

    final Calendar cal = getCalculatedDate(param);

    if (cal != null)
    {
      Date date = cal.getTime();

      String format = getFormatPattern(param, '{', '}');
      if (format == null)
      {
        format = getFormatPattern(param, '(', ')');
      }

      if (format != null)
      {
        final SimpleDateFormat formatter = new SimpleDateFormat();
        if (hasParam(param, "utc"))
        {
          formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        formatter.applyPattern(format);
        result = formatter.format(date);

        if (hasParam(param, "unix"))
        {
          final SimpleDateFormat unixFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
          if (hasParam(param, "utc"))
          {
            unixFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
          }
          date = unixFormatter.parse(result);
          result = Long.toString(date.getTime() / 1000);
        }
        else if (hasParam(param, "last_of_month"))
        {
          date = setLastofMonth(cal);
          result = formatter.format(date);
        }
      }
    }
    return result;
  }

  private Date setLastofMonth(final Calendar cal)
  {
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
    cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
    cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
    return cal.getTime();
  }

  private String getFormatPattern(final String param, final char start, final char end)
  {
    String format = null;
    int startIndex = param.indexOf(start);
    final int endIndex = param.indexOf(end);
    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex)
    {
      format = param.substring(++startIndex, endIndex);
    }

    return format;
  }

  private boolean hasParam(final String widgetParam, final String paramToFind)
  {
    boolean isUnixFormat = false;
    final String params = removeDateFormat(widgetParam);
    final String[] paramArr = params.split(" ");

    for (final String param : paramArr)
    {
      if (param == null)
      {
        return false;
      }

      isUnixFormat |= param.equals(paramToFind);
    }
    return isUnixFormat;
  }

  private Calendar getCalculatedDate(final String widgetParam)
  {
    final Calendar cal = Calendar.getInstance();

    cal.setTimeInMillis(Clock.currentTimeInMillis());

    final String params = removeDateFormat(widgetParam);
    final String[] paramArr = params.split(" ");

    for (final String param : paramArr)
    {
      if (param == null)
      {
        return null;
      }

      int number = 1;
      int startIndex = param.indexOf('+');
      if (startIndex == -1)
      {
        number = -1;
        startIndex = param.indexOf('-');
        if (startIndex == -1)
        {
          number = 0;
        }
      }

      char timeUnit = 'd';
      if (startIndex != -1)
      {
        for (int index = ++startIndex; index < param.length(); index++)
        {
          final char character = param.charAt(index);
          if (character < 47 || character > 58)
          {
            try
            {
              number = number * Integer.parseInt(param.substring(startIndex, index));
            }
            catch (final NumberFormatException _exception)
            {
              return null;
            }

            if (param.length() > index)
            {
              timeUnit = param.charAt(index);
            }

            break;
          }
        }
      }

      switch (timeUnit)
      {
        case 'd':
          cal.add(Calendar.DATE, number);
          break;
        case 'y':
          cal.add(Calendar.YEAR, number);
          break;
        case 'M':
          cal.add(Calendar.MONTH, number);
          break;
        case 'h':
          cal.add(Calendar.HOUR, number);
          break;
        case 'm':
          cal.add(Calendar.MINUTE, number);
          break;
        case 's':
          cal.add(Calendar.SECOND, number);
          break;
        default:
          return null;
      }
    }

    return cal;
  }

  private String removeDateFormat(final String param)
  {
    String result = null;
    int startIndex = param.indexOf('(');
    int endIndex = param.indexOf(')');

    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex)
    {
      final String format = param.substring(++startIndex, endIndex);
      result = param.replace(format, "");
    }

    startIndex = param.indexOf('{');
    endIndex = param.indexOf('}');

    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex)
    {
      final String format = param.substring(++startIndex, endIndex);
      result = param.replace(format, "");
    }
    return result;
  }

}