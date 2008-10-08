package fitnesse.responders.run.slimResponder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReturnedValueExpectation extends SlimTable.Expectation {
  public ReturnedValueExpectation(String expected, int instructionNumber, int col, int row) {
    super(expected, instructionNumber, col, row);
  }

  protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
    String evaluationMessage;
    String replacedValue = slimTable.replaceSymbols(expectedValue);
    if (value.equals(replacedValue))
      evaluationMessage = String.format("!style_pass(%s)", announceBlank(originalValue));
    else if (replacedValue.length() == 0)
      evaluationMessage = String.format("!style_ignore(%s)", literalizedValue);
    else {
      String expressionMessage = Comparator.evaluate(replacedValue, value, expectedValue);
      if (expressionMessage != null)
        evaluationMessage = expressionMessage;
      else
        evaluationMessage = String.format("!style_fail([%s] expected [%s])", literalizedValue, originalValue);
    }

    return slimTable.replaceSymbolsWithFullExpansion(evaluationMessage);
  }

  private String announceBlank(String originalValue) {
    return originalValue.length() == 0 ? "BLANK" : originalValue;
  }

  static class Comparator {
    private String expression;
    private String value;
    private String originalExpression;
    private static Pattern simpleComparison = Pattern.compile(
      "\\A\\s*_?\\s*((?:[<>]=?)|(?:!=))\\s*(\\d*\\.?\\d+)\\s*\\Z"
    );
    private static Pattern range = Pattern.compile(
      "\\A\\s*(\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(\\d*\\.?\\d+)\\s*\\Z"
    );
    private double v;
    private double arg1;
    private double arg2;
    public String operation;

    static String evaluate(String expression, String value, String originalExpression) {
      Comparator comparator = new Comparator(expression, value, originalExpression);
      return comparator.evaluate();
    }

    private Comparator(String expression, String value, String originalExpression) {
      this.expression = expression;
      this.value = value;
      this.originalExpression = originalExpression;
    }

    private String evaluate() {
      operation = matchSimpleComparison();
      if (operation != null)
        return doSimpleComparison();

      Matcher matcher = range.matcher(expression);
      if (matcher.matches() && canUnpackRange(matcher)) {
        return doRange(matcher);
      } else
        return null;
    }

    private String doRange(Matcher matcher) {
      boolean closedLeft = matcher.group(2).equals("=");
      boolean closedRight = matcher.group(3).equals("=");
      boolean pass = (arg1 < v && v < arg2) || (closedLeft && arg1 == v) || (closedRight && arg2 == v);
      return rangeMessage(pass);
    }

    private String rangeMessage(boolean pass) {
      String[] fragments = originalExpression.replaceAll(" ", "").split("_");
      return String.format("!style_%s(%s%s%s)", pass ? "pass" : "fail", fragments[0], value, fragments[1]);
    }

    private boolean canUnpackRange(Matcher matcher) {
      try {
        arg1 = Double.parseDouble(matcher.group(1));
        arg2 = Double.parseDouble(matcher.group(4));
        v = Double.parseDouble(value);
      } catch (NumberFormatException e) {
        return false;
      }
      return true;
    }

    private String doSimpleComparison
      () {
      if (operation.equals("<"))
        return simpleComparisonMessage(v < arg1);
      else if (operation.equals(">"))
        return simpleComparisonMessage(v > arg1);
      else if (operation.equals(">="))
        return simpleComparisonMessage(v >= arg1);
      else if (operation.equals("<="))
        return simpleComparisonMessage(v <= arg1);
      else if (operation.equals("!="))
        return simpleComparisonMessage(v != arg1);
      else
        return null;
    }

    private String simpleComparisonMessage( boolean pass) {
      return String.format("!style_%s(%s%s)", pass ? "pass" : "fail", value, originalExpression.replaceAll(" ", ""));
    }

    private String matchSimpleComparison
      () {
      Matcher matcher = simpleComparison.matcher(expression);
      if (matcher.matches()) {
        try {
          v = Double.parseDouble(value);
          arg1 = Double.parseDouble(matcher.group(2));
          return matcher.group(1);
        } catch (NumberFormatException e1) {
          return null;
        }
      }
      return null;
    }
  }
}
