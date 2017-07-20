// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import fitnesse.components.LogData;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.MockSocket;
import fitnesse.util.SerialExecutorService;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertSubString;

public class FitNesseServerTest {
  private WikiPage root;
  private WikiPagePath pageOnePath;
  private WikiPagePath pageOneTwoPath;
  private FitNesseContext context;

  public FitNesseServerTest() {
  }

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    pageOnePath = PathParser.parse("PageOne");
    pageOneTwoPath = PathParser.parse("PageOne.PageTwo");
  }

  @Test
  public void testSimple() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "some string");
    String output = getSocketOutput("GET /SomePage HTTP/1.1\r\n\r\n");
    String statusLine = "HTTP/1.1 200 OK\r\n";
    assertTrue("Should have statusLine", Pattern.compile(statusLine, Pattern.MULTILINE).matcher(output).find());
    assertTrue("Should have canned Content", hasSubString("some string", output));
  }

  @Test
  public void testNotFound() throws Exception {
    String output = getSocketOutput("GET /WikiWord HTTP/1.1\r\n\r\n");

    assertSubString("Page doesn't exist.", output);
  }

  @Test
  public void testBadRequest() throws Exception {
    String output = getSocketOutput("Bad Request \r\n\r\n");

    assertSubString("400 Bad Request", output);
    assertSubString("The request string is malformed and can not be parsed", output);
  }

  @Test
  public void testSomeOtherPage() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "Page One Content");
    String output = getSocketOutput("GET /PageOne HTTP/1.1\r\n\r\n");
    String expected = "Page One Content";
    assertTrue("Should have page one", hasSubString(expected, output));
  }

  @Test
  public void testSecondLevelPage() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "Page One Content");
    WikiPageUtil.addPage(root, pageOneTwoPath, "Page Two Content");
    String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n");

    String expected = "Page Two Content";
    assertTrue("Should have page Two", hasSubString(expected, output));
  }

  @Test
  public void testRelativeAndAbsoluteLinks() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "PageOne");
    WikiPageUtil.addPage(root, pageOneTwoPath, "PageTwo");
    String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n");
    String expected = "href=\"PageOne.PageTwo\".*PageTwo";
    assertTrue("Should have relative link", hasSubString(expected, output));

    WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "PageTwo at root");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageThree"), "PageThree has link to .PageTwo at the root");
    output = getSocketOutput("GET /PageOne.PageThree HTTP/1.1\r\n\r\n");
    expected = "href=\"PageTwo\".*[.]PageTwo";
    assertTrue("Should have absolute link", hasSubString(expected, output));
  }

  @Test
  public void testLoggingDataCreation() throws Exception {
    MockRequest request = new MockRequest();
    SimpleResponse response = new SimpleResponse(200);
    MockSocket socket = new MockSocket("something");

    socket.setHost("1.2.3.4");
    request.setRequestLine("GET / HTTP/1.1");
    response.setContent("abc");
    request.setCredentials("billy", "bob");

    LogData data = FitNesseExpediter.makeLogData(socket, request, response);

    assertEquals("1.2.3.4", data.host);
    assertNotNull(data.time);
    assertEquals("GET / HTTP/1.1", data.requestLine);
    assertEquals(200, data.status);
    assertEquals(3, data.size);
    assertEquals("billy", data.username);
  }

  private String getSocketOutput(String requestLine) throws Exception {
    MockSocket s = new MockSocket(requestLine);
    ExecutorService executorService = new SerialExecutorService();

    FitNesseServer server = new FitNesseServer(context, executorService);
    server.serve(s, 1000);
    return ((ByteArrayOutputStream) s.getOutputStream()).toString(FileUtil.CHARENCODING);
  }

  private static boolean hasSubString(String expected, String output) {
    return Pattern.compile(expected, Pattern.MULTILINE).matcher(output).find();
  }

}
