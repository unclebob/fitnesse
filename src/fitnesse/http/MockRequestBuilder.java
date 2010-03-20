package fitnesse.http;

import fitnesse.http.MockRequest;
import fitnesse.http.Request;

public class MockRequestBuilder {
  protected String specification;
  
  public MockRequestBuilder(String specification) {
    this.specification = specification;
    validate();
  }

  public Request build() {
    Request request = new MockRequest();
    request.parseRequestUri(getCommand());
    if (hasCredentials()) {
      request.setCredentials(getUsername(), getPassword());
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
      throw new IllegalArgumentException("Command specification [" + specification + "] invalid. Format shold be /cmd or user:pass:/cmd");
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
}
