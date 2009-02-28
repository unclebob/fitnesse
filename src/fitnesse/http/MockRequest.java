// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

public class MockRequest extends Request {
  private Exception parseException = null;

  public MockRequest() {
    super.setResource("");
  }

  public void setRequestUri(String value) {
    requestURI = value;
  }

  public void setRequestLine(String value) {
    requestLine = value;
  }

  public void setResource(String value) {
    super.setResource(value);
  }

  public void setBody(String value) {
    entityBody = value;
  }

  public void setQueryString(String value) {
    queryString = value;
    parseQueryString(value);
  }

  public void addInput(String key, Object value) {
    inputs.put(key, value);
  }

  public void addHeader(String key, Object value) {
    headers.put(key.toLowerCase(), value);
  }

  public void throwExceptionOnParse(Exception e) {
    parseException = e;
  }

  public void getCredentials() {
    return;
  }

  public void setCredentials(String username, String password) {
    authorizationUsername = username;
    authorizationPassword = password;

  }

  public void parse() throws Exception {
    if (parseException != null) {
      throw parseException;
    }
  }
}
