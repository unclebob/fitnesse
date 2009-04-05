// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.components.Base64;
import fitnesse.responders.editing.EditResponder;

public class RequestTest extends TestCase {
  PipedOutputStream output;
  Request request;
  public Thread parseThread;
  public Exception exception;
  ByteArrayOutputStream messageBuffer;

  public void setUp() throws Exception {
    output = new PipedOutputStream();
    request = new Request(new PipedInputStream(output));
    messageBuffer = new ByteArrayOutputStream();
  }

  public void tearDown() throws Exception {
    output.close();
  }

  private void appendToMessage(StringBuffer buffer) throws Exception {
    messageBuffer.write(buffer.toString().getBytes());
  }

  private void appendToMessage(String value) throws Exception {
    messageBuffer.write(value.getBytes());
  }

  private void appendToMessage(byte[] bytes) throws Exception {
    messageBuffer.write(bytes);
  }

  private void parseMessage() throws Exception {
    ByteArrayInputStream stream = new ByteArrayInputStream(messageBuffer.toByteArray());
    request = new Request(stream);
    try {
      request.parse();
    } catch(Exception record) {
      exception = record;
    }
  }

  public void testMultilevelRequest() throws Exception {
    appendToMessage("GET /SomePage.SubPage?edit HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertEquals("SomePage.SubPage", request.getResource());
  }

  public void testSimpleRequest() throws Exception {
    assertFalse(request.hasBeenParsed());
    appendToMessage("GET /request-uri HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertTrue(request.hasBeenParsed());
    assertEquals("/request-uri", request.getRequestUri());
  }

  public void testMalformedRequestLine() throws Exception {
    appendToMessage("/resource HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertNotNull("no exception was thrown", exception);
    assertEquals("The request string is malformed and can not be parsed", exception.getMessage());
  }

  public void testBadMethod() throws Exception {
    appendToMessage("DELETE /resource HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertNotNull("no exception was thrown", exception);
    assertEquals("The DELETE method is not currently supported", exception.getMessage());
  }

  public void testQueryStringValueWithNoQueryString() throws Exception {
    appendToMessage("GET /resource HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertEquals("", request.getQueryString());
  }

  public void testParsingRequestUri() throws Exception {
    appendToMessage("GET /resource?queryString HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertEquals("resource", request.getResource());
    assertEquals("queryString", request.getQueryString());
  }

  public void testCanGetQueryStringValues() throws Exception {
    appendToMessage("GET /resource?key1=value1&key2=value2 HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    checkInputs();
  }

  public void testHeaders() throws Exception {
    appendToMessage("GET /something HTTP/1.1\r\n");
    appendToMessage("Content-Length: 0\r\n");
    appendToMessage("Accept: text/html\r\n");
    appendToMessage("Connection: Keep-Alive\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertEquals(true, request.hasHeader("Content-Length"));
    assertEquals("0", request.getHeader("Content-Length"));
    assertEquals(true, request.hasHeader("Accept"));
    assertEquals("text/html", request.getHeader("Accept"));
    assertEquals(true, request.hasHeader("Connection"));
    assertEquals("Keep-Alive", request.getHeader("Connection"));
    assertEquals(false, request.hasHeader("Something-Else"));
    assertEquals(null, request.getHeader("Something-Else"));
  }

  public void testEntityBodyWithoutContentLength() throws Exception {
    appendToMessage("GET /something HTTP/1.1\r\n");
    appendToMessage("\r\nThis is the Entity Body");
    parseMessage();
    assertEquals("", request.getBody());
  }

  public void testEntityBodyIsRead() throws Exception {
    appendToMessage("GET /something HTTP/1.1\r\n");
    appendToMessage("Content-Length: 23\r\n");
    appendToMessage("\r\n");
    appendToMessage("This is the Entity Body");
    parseMessage();
    assertEquals("This is the Entity Body", request.getBody());
  }

  public void testEntityBodyParametersAreParsed() throws Exception {
    appendToMessage("GET /something HTTP/1.1\r\n");
    appendToMessage("Content-Length: 23\r\n");
    appendToMessage("\r\n");
    appendToMessage("key1=value1&key2=value2");
    parseMessage();
    checkInputs();
  }

  private void checkInputs() {
    assertEquals(true, request.hasInput("key1"));
    assertEquals("value1", request.getInput("key1"));
    assertEquals(true, request.hasInput("key2"));
    assertEquals("value2", request.getInput("key2"));
    assertEquals(false, request.hasInput("someOtherKey"));
    assertEquals(null, request.getInput("someOtherKey"));
  }

  public void testPostMethod() throws Exception {
    appendToMessage("POST /something HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertNull("POST method should be allowed", exception);
  }

  public void testSimpleInputStyle() throws Exception {
    appendToMessage("GET /abc?something HTTP/1.1\r\n");
    appendToMessage("\r\n");
    parseMessage();
    assertEquals(true, request.hasInput("something"));
  }

  public void testOperaPostRequest() throws Exception {
    appendToMessage("POST /HelloThere HTTP/1.1\r\n");
    appendToMessage("User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.02  [en]\r\n");
    appendToMessage("Host: localhost:75\r\n");
    appendToMessage("Accept: text/html, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1\r\n");
    appendToMessage("Accept-Language: en\r\n");
    appendToMessage("Accept-Charset: windows-1252, utf-8, utf-16, iso-8859-1;q=0.6, *;q=0.1\r\n");
    appendToMessage("Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0\r\n");
    appendToMessage("Referer: http://localhost:75/HeloThere?edit=\r\n");
    appendToMessage("Connection: Keep-Alive, TE\r\n");
    appendToMessage("TE: deflate, gzip, chunked, identity, trailers\r\n");
    appendToMessage("Content-type: application/x-www-form-urlencoded\r\n");
    appendToMessage("Content-length: 67\r\n");
    appendToMessage("\r\n");
    appendToMessage(EditResponder.TIME_STAMP+"=1046584670887&Edit=on&Search=on&Test=on&Suite=on&content=abc");

    parseMessage();

    assertTrue(request.hasInput(EditResponder.TIME_STAMP));
    assertTrue(request.hasInput("Edit"));
    assertTrue(request.hasInput("Search"));
    assertTrue(request.hasInput("Test"));
    assertTrue(request.hasInput("Suite"));
    assertTrue(request.hasInput("content"));
  }

  public void testBigPosts() throws Exception {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 1000; j++)
        buffer.append(i);
    }
    String prefix = EditResponder.TIME_STAMP + "=12345&content=";
    appendToMessage("POST /HelloThere HTTP/1.1\r\n");
    appendToMessage(String.format("Content-length: %d\r\n", prefix.length()+buffer.length()));
    appendToMessage("\r\n");
    appendToMessage(prefix);
    appendToMessage(buffer);

    parseMessage();

    String expected = buffer.toString();
    String actual = (String)request.getInput("content");
    assertEquals(expected.length(), actual.length());
    assertEquals(expected, actual);
  }

  public void testMultiPartForms() throws Exception {
    String content = "----bob\r\n" +
      "Content-Disposition: form-data; name=\"key1\"\r\n" +
      "\r\n" +
      "value1\r\n" +
      "----bob\r\n" +
      "Content-Disposition: form-data; name=\"key3\"\r\n" +
      "\r\n" +
      "some\r\nmulti-line\r\nvalue\r\n\r\n" +
      "----bob\r\n" +
      "Content-Disposition: form-data; name=\"key2\"\r\n" +
      "\r\n" +
      "value2\r\n" +
      "----bob\r\n" +
      "Content-Disposition: form-data; name=\"key4\"\r\n" +
      "\r\n" +
      "\r\n" +
      "----bob--\r\n";

    appendToMessage("GET /request-uri HTTP/1.1\r\n");
    appendToMessage("Content-Length: " + content.length() + "\r\n");
    appendToMessage("Content-Type: multipart/form-data; boundary=--bob\r\n");
    appendToMessage("\r\n");
    appendToMessage(content);
    parseMessage();

    if (exception != null) {
      throw exception;
    }
    checkInputs();
    assertEquals(true, request.hasInput("key3"));
    assertEquals("some\r\nmulti-line\r\nvalue\r\n", request.getInput("key3"));

    assertEquals(true, request.hasInput("key4"));
    assertEquals("", request.getInput("key4"));
  }

  public void testUploadingFile() throws Exception {
    String content = "----bob\r\n" +
      "Content-Disposition: form-data; name=\"file1\"; filename=\"mike dile.txt\"\r\n" +
      "Content-Type: text/plain\r\n" +
      "\r\n" +
      "file contents\r\n" +
      "----bob--\r\n";

    appendToMessage("GET /request-uri HTTP/1.1\r\n");
    appendToMessage("Content-Length: " + content.length() + "\r\n");
    appendToMessage("Content-Type: multipart/form-data; boundary=--bob\r\n");
    appendToMessage("\r\n");
    appendToMessage(content);
    parseMessage();

    testUploadedFile("file1", "mike dile.txt", "text/plain", "file contents");
  }

  public void testUploadingTwoFiles() throws Exception {
    String content = "-----------------------------7d32df3a80058\r\n" +
      "Content-Disposition: form-data; name=\"file\"; filename=\"C:\\test.txt\"\r\n" +
      "Content-Type: text/plain\r\n" +
      "\r\n" +
      "test\r\n" +
      "-----------------------------7d32df3a80058\r\n" +
      "Content-Disposition: form-data; name=\"file2\"; filename=\"C:\\test2.txt\"\r\n" +
      "Content-Type: text/plain\r\n" +
      "\r\n" +
      "test2\r\n" +
      "-----------------------------7d32df3a80058--\r\n";

    appendToMessage("GET /request-uri HTTP/1.1\r\n");
    appendToMessage("Content-Length: " + content.length() + "\r\n");
    appendToMessage("Content-Type: multipart/form-data; boundary=---------------------------7d32df3a80058\r\n");
    appendToMessage("\r\n");
    appendToMessage(content);
    parseMessage();

    testUploadedFile("file", "C:\\test.txt", "text/plain", "test");
    testUploadedFile("file2", "C:\\test2.txt", "text/plain", "test2");
  }

  private void testUploadedFile(String name, String filename, String contentType, String content) throws Exception {
    assertEquals(true, request.hasInput(name));
    UploadedFile file = (UploadedFile) request.getInput(name);
    assertNotNull(file);
    assertEquals(filename, file.getName());
    assertEquals(contentType, file.getType());
    assertEquals(content, FileUtil.getFileContent(file.getFile()));
  }

  public void testUploadingBinaryFile() throws Exception {
    appendToMessage("GET /request-uri HTTP/1.1\r\n");
    appendToMessage("Content-Length: " + (83) + "\r\n");
    appendToMessage("Content-Type: multipart/form-data; boundary=--bob\r\n");
    appendToMessage("\r\n");
    appendToMessage("----bob\r\n");
    appendToMessage("Content-Disposition: name=\"n\"; filename=\"f\"\r\n");
    appendToMessage("\r\n");
    appendToMessage(new byte[]{(byte) 0x9D, (byte) 0x90, (byte) 0x81});
    appendToMessage("file contents");
    appendToMessage("\r\n");
    appendToMessage("----bob--");

    parseMessage();

    UploadedFile file = (UploadedFile) request.getInput("n");
    assertNotNull(file);

    byte[] contents = FileUtil.getFileBytes(file.getFile());
    assertEquals((byte) 0x9D, contents[0]);
    assertEquals((byte) 0x90, contents[1]);
    assertEquals((byte) 0x81, contents[2]);
    assertEquals("file contents", new String(contents, 3, contents.length - 3));
  }

  public void testCanGetCredentials() throws Exception {
    appendToMessage("GET /abc?something HTTP/1.1\r\n");
    appendToMessage("Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==\r\n");
    appendToMessage("\r\n");
    parseMessage();
    request.getCredentials();
    assertEquals("Aladdin", request.getAuthorizationUsername());
    assertEquals("open sesame", request.getAuthorizationPassword());
  }

  public void testDoenstChokeOnMissingPassword() throws Exception {
    appendToMessage("GET /abc?something HTTP/1.1\r\n");
    appendToMessage("Authorization: Basic " + Base64.encode("Aladin") + "\r\n");
    appendToMessage("\r\n");
    parseMessage();
    request.getCredentials();
  }

  public void testGetUserpass() throws Exception {
    assertEquals("Aladdin:open sesame", request.getUserpass("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
  }

  public void testUnicodeCharacters() throws Exception {
    appendToMessage("GET /?key=%EB%AA%80%EB%AA%81%EB%AA%82%EB%AA%83 HTTP/1.1\r\n\r\n");
    parseMessage();
    assertEquals("\uba80\uba81\uba82\uba83", request.getInput("key"));
  }

  public void testParsingProgress() throws Exception {
    appendToMessage("GET /something HTTP/1.1\r\n");
    appendToMessage("Content-Length: 23\r\n");
    appendToMessage("\r\n");
    appendToMessage("This is ");
    appendToMessage("the Entity Body");
    parseMessage();
    assertEquals(70, request.numberOfBytesParsed());
  }
}
