package fitnesse.slimTables.responses;

public class PlainResponse implements Response {

  private final String contents;
  private final Response response;

  public PlainResponse(String content) {
    this.contents = content;
    this.response = null;
  }
  public PlainResponse(String originalContent, Response response) {
    this.contents = originalContent;
    this.response = response;
  }
  @Override
  public String toHtml() {
    if (response != null) {
      return contents + " " + response.toHtml();
    }
    return contents;
  }

  public String toString() {
    if (response != null) {
      return contents + " " + response.toString();
    }
    return contents;
  }
}
