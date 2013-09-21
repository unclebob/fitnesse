package fitnesse.slim.protocol;

import fitnesse.slim.SlimError;

public class SyntaxError extends SlimError {
  private static final long serialVersionUID = 1L;

  public SyntaxError(String s) {
    super(s);
  }

  public SyntaxError(Throwable e) {
    super(e);
  }
}
