package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedExpression {
    public static final Pattern formatParser = Pattern.compile("^[ \\t]*((%[-#+ 0,(]*[0-9.]*([a-zA-Z])[^: \\t]*)[ \\t]*:)?[ \\t]*(.*)$");

    private String input;
    private String format;
    private char conversion = '?';
    private String expression;
    private Maybe<String> formatLocale;
    private Locale locale;

    public FormattedExpression(String input, Maybe<String> formatLocale) {
        this.input = input;
        this.formatLocale = formatLocale;
    }

    public Maybe<String> evaluate() {
        parseFormat();
        selectLocale();
        return !expression.isEmpty() ? evaluateAndFormat() : new Maybe<>("");
    }

    private void selectLocale() {
        if(formatLocale.isNothing() || formatLocale.getValue().isEmpty()) {
            locale = Locale.getDefault();
        } else {
            locale = Locale.forLanguageTag(formatLocale.getValue());
        }
    }

    private void parseFormat() {
        Matcher match = formatParser.matcher(input);
        if (match.find()) {  //match.group(1) is an outer group.
            format = (match.group(2) == null) ? null : match.group(2).trim();
            conversion = (match.group(3) == null) ? '?' : match.group(3).trim().charAt(0);
            expression = (match.group(4) == null) ? "" : match.group(4).trim();
        } else {
            format = null;
            expression = input.trim();
        }
    }

    private Maybe<String> evaluateAndFormat() {
        Maybe<Double> evaluation = (new Expression(expression)).evaluate();
        if (evaluation.isNothing()) {
            return Maybe.nothingBecause("invalid expression: " + expression);
        }

        Double result = evaluation.getValue();
        Long iResult = Math.round(result);

        if (format == null)
            return new Maybe<>(result.equals(iResult.doubleValue()) ? iResult.toString() : result.toString());

        if ("aAdhHoOxX".indexOf(conversion) >= 0) //...use the integer
            return new Maybe<>(String.format(locale, format, iResult));

        if ("bB".indexOf(conversion) >= 0) //...use boolean
            return new Maybe<>(result == 0.0 ? "false" : "true");

        if ("sScC".indexOf(conversion) >= 0) { //...string & character formatting; use the double
            String sString;
            boolean isInt = result.equals(iResult.doubleValue());
            if (isInt)
                sString = String.format(locale, format, iResult.toString());
            else
                sString = String.format(locale, format, result.toString());

            return new Maybe<>(sString.replaceAll(" ", HtmlUtil.NBSP.html()));
        }

        if ("tT".indexOf(conversion) >= 0) { //...date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(iResult);
            return new Maybe<>(String.format(locale, format, cal.getTime()));
        }

        if ("eEfgG".indexOf(conversion) >= 0)  //...use the double
            return new Maybe<>(String.format(locale, format, result));

        return Maybe.nothingBecause("invalid format: " + format);
    }
}
