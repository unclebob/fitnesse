package fitnesse.testsystems.slim.responses;

public class ErrorResponse implements Response {

  private final String s;
  private String cause;

  public ErrorResponse(String s) {
    this.s = s;
  }

  public ErrorResponse(String cause, String actual) {
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
