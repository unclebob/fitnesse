// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayInputStream;

import util.RegexTestCase;

public class RequestBuilderTest extends RegexTestCase {
  private RequestBuilder builder;

  public void setUp() {
    builder = new RequestBuilder("/");
  }

  public void testDeafultValues() throws Exception {
    builder = new RequestBuilder("/someResource");
    String text = builder.getText();
    assertHasRegexp("GET /someResource HTTP/1.1\r\n", text);
  }

  public void testHostHeader_RFC2616_section_14_23() throws Exception {
    builder = new RequestBuilder("/someResource");
    String text = builder.getText();
    assertSubString("Host: \r\n", text);

    builder.setHostAndPort("some.host.com", 123);
    text = builder.getText();
    assertSubString("Host: some.host.com:123\r\n", text);
  }

  public void testChangingMethod() throws Exception {
    builder.setMethod("POST");
    String text = builder.getText();
    assertHasRegexp("POST / HTTP/1.1\r\n", text);
  }

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

  public void testGETMethodWithInputs() throws Exception {
    builder.addInput("key", "value");
    String text = builder.getText();
    assertSubString("GET /?key=value HTTP/1.1\r\n", text);
  }

  public void testPOSTMethodWithInputs() throws Exception {
    builder.setMethod("POST");
    builder.addInput("key", "value");
    String text = builder.getText();
    assertSubString("POST / HTTP/1.1\r\n", text);
    assertSubString("key=value", text);
  }

  public void testAddingCredentials() throws Exception {
    builder.addCredentials("Aladdin", "open sesame");
    assertSubString("Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", builder.getText());
  }

  public void testGetBoundary() throws Exception {
    String boundary = builder.getBoundary();

    assertEquals(boundary, builder.getBoundary());
    assertFalse(boundary.equals(new RequestBuilder("blah").getBoundary()));
  }

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

  public void testMultipartWithInputStream() throws Exception {
    ByteArrayInputStream input = new ByteArrayInputStream("data from input stream".getBytes());
    builder.addInputAsPart("input", input, 89, "text/html");
    String text = builder.getText();

    assertSubString("Content-Type: text/html", text);
    assertSubString("\r\n\r\ndata from input stream\r\n", text);
  }

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
