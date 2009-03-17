// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.exception.FitMatcherException;

class FitMatcher {
  private String expression;
  private Object parameter;

  public FitMatcher(String expression, Object parameter) {
    this.expression = expression;
    this.parameter = parameter;
  }

  public boolean matches() throws Exception {
    Pattern p = Pattern.compile("\\s*_?\\s*(<|>|<=|>=)\\s*([-+]?[\\d]*\\.?[\\d]+)");
    Matcher m = p.matcher(expression);
    if (m.matches()) {
      String op = m.group(1);
      String operandString = m.group(2);
      double operand = Double.parseDouble(operandString);
      double n = ((Number) parameter).doubleValue();
      if (op.equals("<")) return (n < operand);
      if (op.equals(">")) return (n > operand);
      if (op.equals("<=")) return (n <= operand);
      if (op.equals(">=")) return (n >= operand);
      return false;
    }

    p = Pattern.compile("\\s*([-+]?[\\d]*\\.?[\\d]+)\\s*(<|>|<=|>=)\\s*_\\s*(<|>|<=|>=)\\s*([-+]?[\\d]*\\.?[\\d]+)");
    m = p.matcher(expression);
    if (m.matches()) {
      double a = Double.parseDouble(m.group(1));
      String aop = m.group(2);
      String bop = m.group(3);
      double b = Double.parseDouble(m.group(4));
      double n = ((Number) parameter).doubleValue();

      boolean an = false;
      if (aop.equals("<")) an = a < n;
      if (aop.equals("<=")) an = a <= n;
      if (aop.equals(">")) an = a > n;
      if (aop.equals(">=")) an = a >= n;

      boolean nb = false;
      if (bop.equals("<")) nb = n < b;
      if (bop.equals("<=")) nb = n <= b;
      if (bop.equals(">")) nb = n > b;
      if (bop.equals(">=")) nb = n >= b;

      return an && nb;
    }
    throw new FitMatcherException("Invalid FitMatcher Expression");
  }

  public String message() {
    String message = null;
    String parmString = "<b>" + parameter.toString() + "</b>";
    if (expression.indexOf("_") == -1)
      message = parmString + expression;
    else
      message = expression.replaceFirst("_", parmString);
    return message;
  }
}
