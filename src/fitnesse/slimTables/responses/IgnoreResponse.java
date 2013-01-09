package fitnesse.slimTables.responses;

public class IgnoreResponse implements Response {

  private final String s;

  public IgnoreResponse(String s) {
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
