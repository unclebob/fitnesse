package fitnesse.testsystems.slim.results;

public class IgnoreResult implements Result {

  private final String s;

  public IgnoreResult(String s) {
    super();
    this.s = s;
  }

  @Override
  public String toHtml() {
    // TODO Auto-generated method stub
    return String.format("<span class=\"ignore\">%s</span>", s);
  }

  public String toString() {
    return String.format("ignore(%s)", s);
  }
}
