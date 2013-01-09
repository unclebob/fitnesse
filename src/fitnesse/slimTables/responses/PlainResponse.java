package fitnesse.slimTables.responses;

public class PlainResponse implements Response {

  private final String s;

  public PlainResponse(String s) {
    this.s = s;
  }
  @Override
  public String toHtml() {
    return s;
  }

  public String toString() {
    return s;
  }
}
