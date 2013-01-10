package fitnesse.testsystems.slim.responses;

public class PassResponse implements Response {

  private final String s;

  public PassResponse(String s) {
    this.s = s;
  }
  @Override
  public String toHtml() {
    return String.format("<span class=\"pass\">%s</span>", s);
  }

  public String toString() {
    return String.format("pass(%s)", s);
  }
}
