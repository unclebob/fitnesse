// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

public class Expression {
  /*
   * Strings used for storing expression.
   */
  private String s, x;
  private boolean isInvalid;

  /*
   * Term evaluator for number literals.
   */
  private double term() {
    double ans = 0;
    StringBuffer temp = new StringBuffer();
    while (s.length() > 0 && Character.isDigit(s.charAt(0))) {
      temp.append(Integer.parseInt("" + s.charAt(0)));
      advance();
    }
    if (s.length() > 0 && s.charAt(0) == '.') {
      temp.append('.');
      advance();
      while (s.length() > 0 && Character.isDigit(s.charAt(0))) {
        temp.append(Integer.parseInt("" + s.charAt(0)));
        advance();
      }
    }
    if (s.length() > 0 && (s.charAt(0) == 'e' || s.charAt(0) == 'E')) {
      temp.append('e');
      advance();
      temp.append(s.charAt(0));
      advance();
      while (s.length() > 0 && Character.isDigit(s.charAt(0))) {
        temp.append(Integer.parseInt("" + s.charAt(0)));
        advance();
      }
    }

    if (temp.length() == 0) //...invalid expression term
      isInvalid = true;
    else
      ans = Double.valueOf(temp.toString()).doubleValue();

    return ans;
  }

  /*
   * Parentheses solver.
   */
  private double paren() {
    double ans;
    if (s.charAt(0) == '(') {
      advance();
      ans = add();
      advance();
    } else {
      ans = term();
    }
    return ans;
  }

  /*
   * Exponentiation solver.
   */
  private double exp() {
    boolean neg = false;
    if (s.charAt(0) == '-') {
      neg = true;
      advance();
    }
    double result = paren();
    while (s.length() > 0) {
      if (s.charAt(0) == '^') {
        result = exponentiate(result);
      } else
        break;
    }
    if (neg)
      result *= -1;
    return result;
  }

  private double exponentiate(double result) {
    advance();
    boolean expNeg = false;
    if (s.charAt(0) == '-') {
      expNeg = true;
      advance();
    }
    double e = paren();
    if (result < 0)
      result = exponentiateNegativeNumber(result, expNeg, e);
    else if (expNeg)
      result = Math.exp(-e * Math.log(result));
    else
      result = Math.exp(e * Math.log(result));
    return result;
  }

  private void advance() {
    s = s.substring(1);
  }

  private double exponentiateNegativeNumber(double result, boolean expNeg, double e) {
    if (Math.ceil(e) == e) {
      result = calculateIntegralExponent(result, expNeg, e);
    } else {
      result = Double.NaN;
    }
    return result;
  }

  private double calculateIntegralExponent(double result, boolean expNeg, double e) {
    double x = 1;
    if (expNeg)
      e *= -1;
    if (e == 0)
      result = 1;
    else if (e > 0)
      for (int i = 0; i < e; i++)
        x *= result;
    else
      for (int i = 0; i < -e; i++)
        x /= result;
    result = x;
    return result;
  }

  /*
   * Trigonometric function solver.
   */
  private double trig() {
    double ans = 0;
    boolean found = false;
    if (s.indexOf("sin") == 0) {
      s = s.substring(3);
      ans = Math.sin(trig());
      found = true;
    } else if (s.indexOf("cos") == 0) {
      s = s.substring(3);
      ans = Math.cos(trig());
      found = true;
    } else if (s.indexOf("tan") == 0) {
      s = s.substring(3);
      ans = Math.tan(trig());
      found = true;
    }
    if (!found) {
      ans = exp();
    }
    return ans;
  }

  /*
   * Multiplication, division expression solver.
   */
  private double mul() {
    double ans = trig();
    if (s.length() > 0) {
      while (s.length() > 0) {
        if (s.charAt(0) == '*') {
          advance();
          ans *= trig();
        } else if (s.charAt(0) == '/') {
          advance();
          ans /= trig();
        } else break;
      }
    }
    return ans;
  }

  /*
   * Addition, subtraction expression solver.
   */
  private double add() {
    double ans = mul();
    while (s.length() > 0) {
      if (s.charAt(0) == '+') {
        advance();
        ans += mul();
      } else if (s.charAt(0) == '-') {
        advance();
        ans -= mul();
      } else {
        break;
      }
    }
    return ans;
  }

  /*
   * Public access method to evaluate this expression.
   */
  public double evaluate() throws Exception {
    isInvalid = false;
    s = x.intern();
    double last = add();
    if (isInvalid) throw (new Exception("invalid expression"));
    return last;
  }

  /*
   * Creates new Expression.
   */
  public Expression(String s) {
    // remove white space, assume only spaces or tabs
    x = s.replaceAll("[ \t]", "");
  }

  /*
   * The String value of this Expression.
   */
  public String toString() {
    return x.intern();
  }
}
