package fitnesse.util;

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
      s = s.substring(1);
    }
    if (s.length() > 0 && s.charAt(0) == '.') {
      temp.append('.');
      s = s.substring(1);
      while (s.length() > 0 && Character.isDigit(s.charAt(0))) {
        temp.append(Integer.parseInt("" + s.charAt(0)));
        s = s.substring(1);
      }
    }
    if (s.length() > 0 && (s.charAt(0) == 'e' || s.charAt(0) == 'E')) {
      temp.append('e');
      s = s.substring(1);
      temp.append(s.charAt(0));
      s = s.substring(1);
      while (s.length() > 0 && Character.isDigit(s.charAt(0))) {
        temp.append(Integer.parseInt("" + s.charAt(0)));
        s = s.substring(1);
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
      s = s.substring(1);
      ans = add();
      s = s.substring(1);
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
      s = s.substring(1);
    }
    double ans = paren();
    while (s.length() > 0) {
      if (s.charAt(0) == '^') {
        s = s.substring(1);
        boolean expNeg = false;
        if (s.charAt(0) == '-') {
          expNeg = true;
          s = s.substring(1);
        }
        double e = paren();
        if (ans < 0) {    // if it's negative
          double x = 1;
          if (Math.ceil(e) == e) {  // only raise to an integer
            if (expNeg)
              e *= -1;
            if (e == 0)
              ans = 1;
            else if (e > 0)
              for (int i = 0; i < e; i++)
                x *= ans;
            else
              for (int i = 0; i < -e; i++)
                x /= ans;
            ans = x;
          } else {
            ans = Math.log(-1);  // otherwise make it NaN
          }
        } else if (expNeg)
          ans = Math.exp(-e * Math.log(ans));
        else
          ans = Math.exp(e * Math.log(ans));
      } else
        break;
    }
    if (neg)
      ans *= -1;
    return ans;
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
          s = s.substring(1);
          ans *= trig();
        } else if (s.charAt(0) == '/') {
          s = s.substring(1);
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
        s = s.substring(1);
        ans += mul();
      } else if (s.charAt(0) == '-') {
        s = s.substring(1);
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