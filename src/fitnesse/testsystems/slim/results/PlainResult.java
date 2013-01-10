package fitnesse.testsystems.slim.results;

public class PlainResult implements Result {

  private final String contents;
  private final Result response;
  private final Result secondResponse;

  public PlainResult(String content) {
    this.contents = content;
    this.response = null;
    this.secondResponse = null;
  }

  public PlainResult(String originalContent, Result response) {
    this.contents = originalContent;
    this.response = response;
    this.secondResponse = null;
  }

  public PlainResult(Result response1, Result response2) {
    this.contents = null;
    this.response = response1;
    this.secondResponse = response2;
  }

  @Override
  public String toHtml() {
    if (secondResponse != null) {
      return response.toHtml() + secondResponse.toHtml();
    } else if (response != null) {
      return contents + " " + response.toHtml();
    }
    return contents;
  }

  public String toString() {
    if (secondResponse != null) {
      return response.toString() + secondResponse.toString();
    } else if (response != null) {
      return contents + " " + response.toString();
    }
    return contents;
  }
}
