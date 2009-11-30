// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ResponseTest {
  @Test
  public void reasonCode() throws Exception {
    checkPhrase(100, "Continue");
    checkPhrase(101, "Switching Protocols");
    checkPhrase(200, "OK");
    checkPhrase(201, "Created");
    checkPhrase(202, "Accepted");
    checkPhrase(203, "Non-Authoritative Information");
    checkPhrase(204, "No Content");
    checkPhrase(205, "Reset Content");
    checkPhrase(300, "Multiple Choices");
    checkPhrase(301, "Moved Permanently");
    checkPhrase(302, "Found");
    checkPhrase(303, "See Other");
    checkPhrase(304, "Not Modified");
    checkPhrase(305, "Use Proxy");
    checkPhrase(307, "Temporary Redirect");
    checkPhrase(400, "Bad Request");
    checkPhrase(401, "Unauthorized");
    checkPhrase(402, "Payment Required");
    checkPhrase(403, "Forbidden");
    checkPhrase(404, "Not Found");
    checkPhrase(405, "Method Not Allowed");
    checkPhrase(406, "Not Acceptable");
    checkPhrase(407, "Proxy Authentication Required");
    checkPhrase(408, "Request Time-out");
    checkPhrase(409, "Conflict");
    checkPhrase(410, "Gone");
    checkPhrase(411, "Length Required");
    checkPhrase(412, "Precondition Failed");
    checkPhrase(413, "Request Entity Too Large");
    checkPhrase(414, "Request-URI Too Large");
    checkPhrase(415, "Unsupported Media Type");
    checkPhrase(416, "Requested range not satisfiable");
    checkPhrase(417, "Expectation Failed");
    checkPhrase(500, "Internal Server Error");
    checkPhrase(501, "Not Implemented");
    checkPhrase(502, "Bad Gateway");
    checkPhrase(503, "Service Unavailable");
    checkPhrase(504, "Gateway Time-out");
    checkPhrase(505, "HTTP Version not supported");
    checkPhrase(-1,  "Unknown Status");
  }

  private void checkPhrase(int reasonCode, String reasonPhrase) {
    assertEquals(reasonPhrase, Response.getReasonPhrase(reasonCode));
  }

  class MockResponse extends Response {
    public MockResponse(String formatString) {
      super(formatString);
    }

    public void readyToSend(ResponseSender sender) throws Exception {
    }

    protected void addSpecificHeaders() {
    }

    public int getContentSize() {
      return 0;
    }
  }

  @Test
  public void htmlFormat() throws Exception {
    Response response = new MockResponse("html");
    assertTrue(response.isHtmlFormat());
  }

  @Test
  public void xmlFormat() throws Exception {
    Response response = new MockResponse("xml");
    assertTrue(response.isXmlFormat());    
  }

  @Test
  public void textFormat() throws Exception {
    Response response = new MockResponse("text");
    assertTrue(response.isTextFormat());    
  }

  @Test
  public void defaultFormat() throws Exception {
    Response response = new MockResponse("");
    assertTrue(response.isHtmlFormat());    
  }

  @Test
  public void shouldNotHaveHeadersIfText() throws Exception {
    Response response = new MockResponse("text");
    response.addStandardHeaders();
    assertEquals("", response.makeHttpHeaders());
  }

  @Test
  public void shouldHaveHeadersIfHtml() throws Exception {
    Response response = new MockResponse("html");
    response.addStandardHeaders();
    assertTrue(response.makeHttpHeaders().indexOf("HTTP/1.1 200 OK") != -1);
  }

  @Test
  public void shouldHaveHeadersIfXml() throws Exception {
    Response response = new MockResponse("xml");
    response.addStandardHeaders();
    assertTrue(response.makeHttpHeaders().indexOf("HTTP/1.1 200 OK") != -1);
  }
}
