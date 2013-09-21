// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

public class RequestBuilderTest {
  private RequestBuilder builder;

  @Before
  public void setUp() {
    builder = new RequestBuilder("/");
  }

  @Test
  public void testDeafultValues() throws Exception {
    builder = new RequestBuilder("/someResource");
    String text = builder.getText();
    assertHasRegexp("GET /someResource HTTP/1.1\r\n", text);
  }

  @Test
  public void testHostHeader_RFC2616_section_14_23() throws Exception {
    builder = new RequestBuilder("/someResource");
    String text = builder.getText();
    assertSubString("Host: \r\n", text);

    builder.setHostAndPort("some.host.com", 123);
    text = builder.getText();
    assertSubString("Host: some.host.com:123\r\n", text);
  }

  @Test
  public void testChangingMethod() throws Exception {
    builder.setMethod("POST");
    String text = builder.getText();
    assertHasRegexp("POST / HTTP/1.1\r\n", text);
  }

  @Test
  public void testAddInput() throws Exception {
    builder.addInput("responder", "saveData");
    String content = "!fixture fit.ColumnFixture\n" +
    "\n" +
    "!path classes\n" +
    "\n" +
    "!2 ";
    builder.addInput("pageContent", content);

    String inputString = builder.inputString();
    assertSubString("responder=saveData", inputString);
    assertSubString("pageContent=%21fixture+fit.ColumnFixture%0A%0A%21path+classes%0A%0A%212+", inputString);
    assertSubString("&", inputString);
  }

  @Test
  public void testGETMethodWithInputs() throws Exception {
    builder.addInput("key", "value");
    String text = builder.getText();
    assertSubString("GET /?key=value HTTP/1.1\r\n", text);
  }

  @Test
  public void testPOSTMethodWithInputs() throws Exception {
    builder.setMethod("POST");
    builder.addInput("key", "value");
    String text = builder.getText();
    assertSubString("POST / HTTP/1.1\r\n", text);
    assertSubString("key=value", text);
  }

  @Test
  public void testAddingCredentials() throws Exception {
    builder.addCredentials("Aladdin", "open sesame");
    assertSubString("Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", builder.getText());
  }

  @Test
  public void testGetBoundary() throws Exception {
    String boundary = builder.getBoundary();

    assertEquals(boundary, builder.getBoundary());
    assertFalse(boundary.equals(new RequestBuilder("blah").getBoundary()));
  }

  @Test
  public void testMultipartOnePart() throws Exception {
    builder.addInputAsPart("myPart", "part data");
    String text = builder.getText();

    assertSubString("POST", text);
    assertSubString("Content-Type: multipart/form-data; boundary=", text);
    String boundary = builder.getBoundary();
    assertSubString("--" + boundary, text);
    assertSubString("\r\n\r\npart data\r\n", text);
    assertSubString("--" + boundary + "--", text);
  }

  @Test
  public void testMultipartWithInputStream() throws Exception {
    ByteArrayInputStream input = new ByteArrayInputStream("data from input stream".getBytes());
    builder.addInputAsPart("input", input, 89, "text/html");
    String text = builder.getText();

    assertSubString("Content-Type: text/html", text);
    assertSubString("\r\n\r\ndata from input stream\r\n", text);
  }

  @Test
  public void testMultipartWithRequestParser() throws Exception {
    builder.addInputAsPart("part1", "data 1");
    builder.addInput("input1", "input1 value");
    builder.addInputAsPart("part2", "data 2");
    String text = builder.getText();

    Request request = new Request(new ByteArrayInputStream(text.getBytes()));
    request.parse();
    assertEquals("data 1", request.getInput("part1"));
    assertEquals("data 2", request.getInput("part2"));
    assertEquals("input1 value", request.getInput("input1"));
  }
}
