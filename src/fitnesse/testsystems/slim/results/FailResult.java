package fitnesse.testsystems.slim.results;

public class FailResult implements Result {

  private final String s;

  public FailResult(String s) {
    this.s = s;
  }

  @Override
  public String toHtml() {
    // TODO Auto-generated method stub
    return String.format("<span class=\"fail\">%s</span>", s);
  }

  public String toString() {
    return String.format("fail(%s)", s);
  }
}
