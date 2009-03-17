// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
// Derived from copyrighted code by Object Mentor, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
//EvaluatorWidget: Created using VariableWidget & Expression
package fitnesse.wikitext.widgets;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Expression;
import fitnesse.html.HtmlUtil;

public class EvaluatorWidget extends ParentWidget {
  public static final String REGEXP = "\\$\\{=[ \\t]*(?:%[-#+ 0,(]*(?:[0-9]*\\.?[0-9]*)?[a-zA-Z]+[ \\t]*:)?[^:=]*=\\}";
  public static final Pattern pattern = Pattern.compile("\\$\\{=([^=]*)=\\}", Pattern.MULTILINE + Pattern.DOTALL);
  public static final Pattern formatParser = Pattern.compile("^[ \\t]*((%[-#+ 0,(]*[0-9.]*([a-zA-Z])[^: \\t]*)[ \\t]*:)?[ \\t]*(.*)$");
  private String name = null;
  private String formatSpec = null;
  private char conversion = '?';
  private String renderedText;
  private boolean rendered;

  public EvaluatorWidget(ParentWidget parent, String text) {
    super(parent);
    name = text;
    Matcher match = pattern.matcher(text);
    if (match.find()) name = match.group(1);
  }

  public String render() throws Exception {
    if (!rendered) doRender();
    return renderedText;
  }

  private void doRender() throws Exception {
    parseOutFormat(expandVariables(name));
    if (renderedText.length() > 0) {
      try {
        evaluateAndFormat();
      }
      catch (Exception e) {
        renderedText = makeInvalidVariableExpression(name);
      }
    }
    rendered = true;
  }

  private void parseOutFormat(String expr) {
    Matcher match = formatParser.matcher(expr);
    if (match.find()) {  //match.group(1) is an outer group.
      formatSpec = (match.group(2) == null) ? null : match.group(2).trim();
      conversion = (match.group(3) == null) ? '?' : match.group(3).trim().charAt(0);
      renderedText = (match.group(4) == null) ? "" : match.group(4).trim();
    } else {
      formatSpec = null;
      renderedText = expr.trim();
    }
  }

  private void evaluateAndFormat() throws Exception {
    Double result = (new Expression(renderedText)).evaluate();
    Long iResult = new Long(Math.round(result));

    if (formatSpec == null)
      renderedText = (result.equals(iResult.doubleValue())) ? iResult.toString() : result.toString();
    else {
      //char conversion = formatSpec.charAt(formatSpec.length() - 1);

      if ("aAdhHoOxX".indexOf(conversion) >= 0) //...use the integer
        renderedText = String.format(formatSpec, iResult);
      else if ("bB".indexOf(conversion) >= 0) //...use boolean
        renderedText = (result == 0.0) ? "false" : "true";
      else if ("sScC".indexOf(conversion) >= 0) //...string & character formatting; use the double
      {
        String sString;
        boolean isInt = result.equals(iResult.doubleValue());
        if (isInt)
          sString = String.format(formatSpec, iResult.toString());
        else
          sString = String.format(formatSpec, result.toString());

        renderedText = sString.replaceAll(" ", HtmlUtil.NBSP.html());
      } else if ("tT".indexOf(conversion) >= 0) //...date
      {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(iResult);
        renderedText = String.format(formatSpec, cal.getTime());
      } else if ("eEfgG".indexOf(conversion) >= 0)  //...use the double
        renderedText = String.format(formatSpec, result);
      else
        renderedText = makeInvalidVariableExpression("invalid format: " + formatSpec);
    }
  }

  private String makeInvalidVariableExpression(String name) throws Exception {
    return HtmlUtil.metaText("invalid expression: " + name);
  }

  public String asWikiText() throws Exception {
    return "${=" + name + "=}";
  }
}


