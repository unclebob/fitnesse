// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

public class MockRequest extends Request {

  public MockRequest() {
    super.setResource("");
  }

  public void setRequestLine(String value) {
    requestLine = value;
  }

  public void setBody(String value) {
    entityBody = value;
  }

  public void setQueryString(String value) {
    queryString = value;
    parseQueryString(value);
  }

  public void addInput(String key, String value) {
    inputs.put(key, value);
  }

  public void addHeader(String key, String value) {
    headers.put(key.toLowerCase(), value);
  }

  public void addUploadedFile(String name, UploadedFile uploadedFile) {
    uploadedFiles.put(name, uploadedFile);
  }

  public void getCredentials() {
  }

  public void setCredentials(String username, String password) {
    authorizationUsername = username;
    authorizationPassword = password;

  }

  public void parse() {
  }
}
