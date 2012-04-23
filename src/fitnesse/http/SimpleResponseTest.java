// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import util.RegexTestCase;

public class SimpleResponseTest extends RegexTestCase implements ResponseSender {
  private StringBuffer buffer;
  private String text;
  private boolean closed = false;

  public void send(byte[] bytes) {
    try {
      buffer.append(new String(bytes, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    text = buffer.toString();
  }

  @Override
  public void close() {
    closed = true;
  }

  @Override
  public Socket getSocket() {
    return null;
  }

  public void setUp() throws Exception {
    buffer = new StringBuffer();
    text = null;
  }

  public void tearDown() throws Exception {
  }

  public void testSimpleResponse() {
    SimpleResponse response = new SimpleResponse();
    response.setContent("some content");
    response.sendTo(this);
    assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
    assertHasRegexp("Content-Length: 12", text);
    assertHasRegexp("Content-Type: text/html", text);
    assertTrue(text.endsWith("some content"));
    assertTrue(closed);
  }

  public void testPageNotFound() throws Exception {
    SimpleResponse response = new SimpleResponse(404);
    response.sendTo(this);
    assertHasRegexp("404 Not Found", text);
  }

  public void testRedirect() throws Exception {
    SimpleResponse response = new SimpleResponse();
    response.redirect("some url");
    response.sendTo(this);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: some url\r\n", text);
  }

  public void testUnicodeCharacters() {
    SimpleResponse response = new SimpleResponse();
    response.setContent("\uba80\uba81\uba82\uba83");
    response.sendTo(this);

    assertSubString("\uba80\uba81\uba82\uba83", text);
  }
}



