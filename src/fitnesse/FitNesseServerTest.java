// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.wiki.*;
import fitnesse.testutil.*;
import fitnesse.responders.*;
import fitnesse.responders.files.*;
import fitnesse.components.*;
import fitnesse.http.*;
import fitnesse.util.FileUtil;

import java.util.regex.Pattern;

public class FitNesseServerTest extends RegexTest
{
	private PageCrawler crawler;
	private WikiPage root;
	private WikiPagePath pageOnePath;
	private WikiPagePath pageOneTwoPath;

	public FitNesseServerTest()
	{
	}

	public void setUp() throws Exception
  {
    SampleFileUtility.makeSampleFiles();
	  root = InMemoryPage.makeRoot("RootPage");
	  crawler = root.getPageCrawler();
	  pageOnePath = PathParser.parse("PageOne");
	  pageOneTwoPath = PathParser.parse("PageOne.PageTwo");
  }

  public void tearDown() throws Exception
  {
    FileUtil.deleteFileSystemDirectory(SampleFileUtility.base);
  }

  public void testSimple() throws Exception
  {
    crawler.addPage(root, PathParser.parse("SomePage"), "some string");
    String output = getSocketOutput("GET /SomePage HTTP/1.1\r\n\r\n", root);
    String statusLine = "HTTP/1.1 200 OK\r\n";
    assertTrue("Should have statusLine", Pattern.compile(statusLine, Pattern.MULTILINE).matcher(output).find());
    assertTrue("Should have canned Content", hasSubString("some string", output));
  }

  public void testNotFound() throws Exception
  {
    String output = getSocketOutput("GET /WikiWord HTTP/1.1\r\n\r\n", new MockWikiPage());

    assertSubString("404 Not Found", output);
  }

  public void testBadRequest() throws Exception
  {
    String output = getSocketOutput("Bad Request \r\n\r\n", new MockWikiPage());

    assertSubString("400 Bad Request", output);
	  assertSubString("The request string is malformed and can not be parsed", output);
  }

  public void testFrontPageRequest() throws Exception
  {
    crawler.addPage(root, PathParser.parse("FrontPage"), "This is the FrontPage content");
    String output = getSocketOutput("GET / HTTP/1.1\r\n\r\n", root);
    String expected = "This is the .* content";
    assertTrue("Should have content", hasSubString(expected, output));
  }

  public void testSomeOtherPage() throws Exception
  {
    crawler.addPage(root, pageOnePath, "Page One Content");
    String output = getSocketOutput("GET /PageOne HTTP/1.1\r\n\r\n", root);
    String expected = "Page One Content";
    assertTrue("Should have page one", hasSubString(expected, output));
  }

  public void testSecondLevelPage() throws Exception
  {
    crawler.addPage(root, pageOnePath, "Page One Content");
    crawler.addPage(root, pageOneTwoPath, "Page Two Content");
    String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n", root);

    String expected = "Page Two Content";
    assertTrue("Should have page Two", hasSubString(expected, output));
  }

  public void testRelativeAndAbsoluteLinks() throws Exception
  {
    WikiPage root = InMemoryPage.makeRoot("RootPage");
    crawler.addPage(root, pageOnePath, "PageOne");
    crawler.addPage(root, pageOneTwoPath, "PageTwo");
    String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n", root);
    String expected = "href=\"PageOne.PageTwo\".*PageTwo";
    assertTrue("Should have relative link", hasSubString(expected, output));

    crawler.addPage(root, PathParser.parse("PageTwo"), "PageTwo at root");
    crawler.addPage(root, PathParser.parse("PageOne.PageThree"), "PageThree has link to .PageTwo at the root");
    output = getSocketOutput("GET /PageOne.PageThree HTTP/1.1\r\n\r\n", root);
    expected = "href=\"PageTwo\".*[.]PageTwo";
    assertTrue("Should have absolute link", hasSubString(expected, output));
  }

  public void testServingRegularFiles() throws Exception
  {
    String output = getSocketOutput("GET /files/testDir/testFile2 HTTP/1.1\r\n\r\n", new MockWikiPage());
    assertHasRegexp("file2 content", output);
  }

  public void testLoggingDataCreation() throws Exception
  {
    MockRequest request = new MockRequest();
    SimpleResponse response = new SimpleResponse(200);
    MockSocket socket = new MockSocket("something");

    socket.setHost("1.2.3.4");
    request.setRequestLine("GET / HTTP/1.1");
    response.setContent("abc");

    LogData data = FitNesseExpediter.makeLogData(socket, request, response);

    assertEquals("1.2.3.4", data.host);
    assertNotNull(data.time);
    assertEquals("GET / HTTP/1.1", data.requestLine);
    assertEquals(200, data.status);
    assertEquals(3, data.size);
  }

  private String getSocketOutput(String requestLine, WikiPage page) throws Exception
  {
    MockSocket s = new MockSocket(requestLine);
    FitNesseContext context = new FitNesseContext();
    context.rootPagePath = SampleFileUtility.base;
    context.responderFactory = new ResponderFactory(SampleFileUtility.base);
    context.root = page;
    FitNesseServer server = new FitNesseServer(context);
    server.serve(s, 1000);
    String output = s.getOutput();
    return output;
  }

  private static boolean hasSubString(String expected, String output)
  {
    return Pattern.compile(expected, Pattern.MULTILINE).matcher(output).find();
  }
}
