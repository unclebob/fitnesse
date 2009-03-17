// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChunkedResponseTest implements ResponseSender {
  private ChunkedResponse response;
  private boolean closed = false;

  public StringBuffer buffer;

  public void send(byte[] bytes) throws Exception {
    buffer.append(new String(bytes, "UTF-8"));
  }

  public void close() {
    closed = true;
  }

  public Socket getSocket() throws Exception {
    return null;
  }

  @Before
  public void setUp() throws Exception {
    buffer = new StringBuffer();

    response = new ChunkedResponse("html");
    response.readyToSend(this);
  }

  @After
  public void tearDown() throws Exception {
    response.closeAll();
  }

  @Test
  public void testHeaders() throws Exception {
    assertTrue(response.isReadyToSend());
    String text = buffer.toString();
    assertHasRegexp("Transfer-Encoding: chunked", text);
    assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
    assertHasRegexp("Content-Type: text/html", text);
  }

  @Test
  public void xmlHeaders() throws Exception {
    response = new ChunkedResponse("xml");
    response.readyToSend(this);
    assertTrue(response.isReadyToSend());
    assertTrue(response.isReadyToSend());
    String text = buffer.toString();
    assertHasRegexp("Transfer-Encoding: chunked", text);
    assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
    assertHasRegexp("Content-Type: text/xml", text);
  }

  @Test
  public void testOneChunk() throws Exception {
    buffer = new StringBuffer();
    response.add("some more text");

    String text = buffer.toString();
    assertEquals("e\r\nsome more text\r\n", text);
  }

  @Test
  public void testTwoChunks() throws Exception {
    buffer = new StringBuffer();
    response.add("one");
    response.add("two");

    String text = buffer.toString();
    assertEquals("3\r\none\r\n3\r\ntwo\r\n", text);
  }

  @Test
  public void testSimpleClosing() throws Exception {
    assertFalse(closed);
    buffer = new StringBuffer();
    response.closeAll();
    String text = buffer.toString();
    assertEquals("0\r\n\r\n", text);
    assertTrue(closed);
  }

  @Test
  public void testClosingInSteps() throws Exception {
    assertFalse(closed);
    buffer = new StringBuffer();
    response.closeChunks();
    assertEquals("0\r\n", buffer.toString());
    assertFalse(closed);
    buffer = new StringBuffer();
    response.closeTrailer();
    assertEquals("\r\n", buffer.toString());
    assertFalse(closed);
    response.close();
    assertTrue(closed);
  }

  @Test
  public void testContentSize() throws Exception {
    response.add("12345");
    response.closeAll();
    assertEquals(5, response.getContentSize());
  }

  @Test
  public void testNoNullPointerException() throws Exception {
    String s = null;
    try {
      response.add(s);
    }
    catch (Exception e) {
      fail("should not throw exception");
    }
  }

  @Test
  public void testTrailingHeaders() throws Exception {
    response.closeChunks();
    buffer = new StringBuffer();
    response.addTrailingHeader("Some-Header", "someValue");
    assertEquals("Some-Header: someValue\r\n", buffer.toString());
    response.closeTrailer();
    response.close();
    assertTrue(closed);
  }

  @Test
  public void testCantAddZeroLengthBytes() throws Exception {
    int originalLength = buffer.length();
    response.add("");
    assertEquals(originalLength, buffer.length());
    response.closeAll();
  }

  @Test
  public void testUnicodeCharacters() throws Exception {
    response.add("\uba80\uba81\uba82\uba83");
    response.closeAll();

    assertSubString("\uba80\uba81\uba82\uba83", buffer.toString());
  }
}
