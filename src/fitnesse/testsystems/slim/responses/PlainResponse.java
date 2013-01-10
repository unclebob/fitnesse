package fitnesse.testsystems.slim.responses;

public class PlainResponse implements Response {

  private final String contents;
  private final Response response;
  private final Response secondResponse;

  public PlainResponse(String content) {
    this.contents = content;
    this.response = null;
    this.secondResponse = null;
  }

  public PlainResponse(String originalContent, Response response) {
    this.contents = originalContent;
    this.response = response;
    this.secondResponse = null;
  }

  public PlainResponse(Response response1, Response response2) {
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
