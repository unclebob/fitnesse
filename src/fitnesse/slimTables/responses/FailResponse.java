package fitnesse.slimTables.responses;

public class FailResponse implements Response {

  private final String s;

  public FailResponse(String s) {
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
