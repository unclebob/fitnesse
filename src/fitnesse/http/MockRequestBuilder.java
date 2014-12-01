package fitnesse.http;

public class MockRequestBuilder {
  protected String specification;
  private boolean chunk = true;

  public MockRequestBuilder(String specification) {
    this.specification = specification;
    validate();
  }

  public Request build() {
    MockRequest request = new MockRequest();
    request.parseRequestUri(getCommand());
    if (hasCredentials()) {
      request.setCredentials(getUsername(), getPassword());
    }
    if (!chunk) {
      request.addInput(Request.NOCHUNK, "true");
    }
    return request;
  }

  private String getCommand() {
    String actualCommand = null;

    if (hasCredentials())
      actualCommand = commandParts()[2];
    else
      actualCommand = specification;


    if (actualCommand.startsWith("/"))
      return actualCommand;
    else
      return "/" + actualCommand;
  }

  private boolean hasCredentials() {
    return (commandParts().length == 3);
  }

  private boolean hasNoCredentials() {
    return (commandParts().length == 1);
  }

  private void validate() {
    if (!hasCredentials() && !hasNoCredentials())
      throw new IllegalArgumentException("Command specification [" + specification + "] invalid. Format should be /cmd or user:pass:/cmd");
  }

  private String[] commandParts() {
    return specification.split(":");
  }

  private String getUsername() {
    return commandParts()[0];
  }

  private String getPassword() {
    return commandParts()[1];
  }

  public MockRequestBuilder noChunk() {
    this.chunk = false;
    return this;
  }
}
