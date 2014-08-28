// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

public class SimpleResponseTest implements ResponseSender {
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

  @Before
  public void setUp() throws Exception {
    buffer = new StringBuffer();
    text = null;
  }

  @Test
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

  @Test
  public void testPageNotFound() throws Exception {
    SimpleResponse response = new SimpleResponse(404);
    response.sendTo(this);
    assertHasRegexp("404 Not Found", text);
  }

  @Test
  public void testRedirect() throws Exception {
    SimpleResponse response = new SimpleResponse();
    response.redirect("", "some url");
    response.sendTo(this);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: some url\r\n", text);
  }

  @Test
  public void testRedirectWithContextRoot() throws Exception {
    SimpleResponse response = new SimpleResponse();
    response.redirect("/contextroot/", "some url");
    response.sendTo(this);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: /contextroot/some url\r\n", text);
  }

  @Test
  public void testUnicodeCharacters() {
    SimpleResponse response = new SimpleResponse();
    response.setContent("\uba80\uba81\uba82\uba83");
    response.sendTo(this);

    assertSubString("\uba80\uba81\uba82\uba83", text);
  }
}



