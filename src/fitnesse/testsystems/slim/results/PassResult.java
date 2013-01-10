package fitnesse.testsystems.slim.results;

public class PassResult implements Result {

  private final String s;

  public PassResult(String s) {
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
