package fitnesse.testsystems.slim.results;

public class ErrorResult implements Result {

  private final String s;
  private String cause;

  public ErrorResult(String s) {
    this.s = s;
  }

  public ErrorResult(String cause, String actual) {
    s = actual;
    this.cause = cause;
  }

  @Override
  public String toHtml() {
    if (cause != null) {
      // "!style_error(Unknown construction message:) " + actual
      return String.format("<span class=\"error\">%s:</span> %s", cause, s);
    }
    return String.format("<span class=\"error\">%s</span>", s);
  }

  public String toString() {
    return String.format("error(%s)", s);
  }
}
