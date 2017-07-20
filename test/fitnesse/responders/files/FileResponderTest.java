// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import fitnesse.FitNesseContext;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.SampleFileUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static fitnesse.responders.files.FileResponder.isInFilesDirectory;
import static org.junit.Assert.*;
import static util.RegexTestCase.*;

public class FileResponderTest {
  MockRequest request;
  private final String HTTP_DATE_REGEXP = "[SMTWF][a-z]{2}\\,\\s[0-9]{2}\\s[JFMASOND][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\sGMT";
  private Response response;
  private FitNesseContext context;
  private FileResponder responder;
  private Locale saveLocale;
  // Example: "Tue, 02 Apr 2003 22:18:49 GMT"

  @Before
  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
    SampleFileUtility.makeSampleFiles(context.getRootPagePath());
    response = null;
    saveLocale = Locale.getDefault();
    FitNesseUtil.makeTestContext();
    responder = new FileResponder();
  }

  @After
  public void tearDown() throws Exception {
    if (response != null) response.sendTo(new MockResponseSender());
    SampleFileUtility.deleteSampleFiles(context.getRootPagePath());
    Locale.setDefault(saveLocale);
  }

  @Test
  public void testFileContent() throws Exception {
    request.setResource("files/testFile1");
    response = responder.makeResponse(context, request);
    assertEquals(InputStreamResponse.class, response.getClass());
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertSubString("file1 content", sender.sentData());
  }

  @Test
  public void testClasspathResourceContent() throws Exception {
    request.setResource("files/fitnesse/testresource.txt");
    response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertSubString("test resource content", sender.sentData());
  }

  @Test
  public void testSpacesInFileName() throws Exception {
    request.setResource("files/test%20File%20With%20Spaces%20In%20Name");
    responder = new FileResponder();
    responder.makeResponse(context, request);
    assertEquals(context.getRootPagePath() + File.separator + "files" + File.separator + "test File With Spaces In Name", responder.requestedFile.getPath());
  }

  @Test
  public void testUrlEncodedSpacesInFileName() throws Exception {
    request.setResource("files/file4%20with%20spaces%32.txt");
    responder.makeResponse(context, request);
    assertEquals("files/file4 with spaces2.txt", responder.resource);
  }

  @Test
  public void testLastModifiedHeader() throws Exception {
    Locale.setDefault(Locale.US);
    request.setResource("files/testFile1");
    response = responder.makeResponse(context, request);
    String lastModifiedHeader = response.getHeader("Last-Modified");
    assertMatches(HTTP_DATE_REGEXP, lastModifiedHeader);
  }

  @Test
  public void testLastModifiedHeaderForClasspathResources() throws Exception {
    Locale.setDefault(Locale.US);
    request.setResource("files/fitnesse/css/fitnesse.css");
    response = responder.makeResponse(context, request);
    String lastModifiedHeader = response.getHeader("Last-Modified");
    assertMatches(HTTP_DATE_REGEXP, lastModifiedHeader);
  }

  private void test304IfNotModified(String resource) throws Exception {
    Locale.setDefault(Locale.US);
    Calendar now = new GregorianCalendar();
    now.add(Calendar.DATE, -1);
    String yesterday = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());
    now.add(Calendar.DATE, 2);
    String tomorrow = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());

    request.setResource(resource);
    request.addHeader("If-Modified-Since", yesterday);
    response = responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    request.setResource(resource);
    request.addHeader("If-Modified-Since", tomorrow);
    SimpleResponse notModifiedResponse = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(304, notModifiedResponse.getStatus());
    assertEquals("", notModifiedResponse.getContent());
    assertMatches(HTTP_DATE_REGEXP, notModifiedResponse.getHeader("Date"));
    assertNotNull(notModifiedResponse.getHeader("Cache-Control"));
    assertNull(notModifiedResponse.getHeader("Content-Type"));
  }

  @Test
  public void test304IfNotModifiedForFiles() throws Exception {
    test304IfNotModified("files/testFile1");
  }

  @Test
  public void test304IfNotModifiedForClasspathResources() throws Exception {
    test304IfNotModified("files/fitnesse/css/fitnesse.css");
  }

  @Test
  public void testRecoverFromUnparseableDateInIfNotModifiedHeader() throws Exception {
    request.setResource("files/testFile1");
    request.addHeader("If-Modified-Since", "Unparseable Date");
    response = responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testNotFoundFile() throws Exception {
    request.setResource("files/something/that/aint/there");
    Response response = responder.makeResponse(context, request);
    assertEquals(404, response.getStatus());
    assertHasRegexp("files/something/that/aint/there", ((SimpleResponse) response).getContent());
  }

  @Test
  public void testCssMimeType() throws Exception {
    SampleFileUtility.addFile(context.getRootPagePath(), "/files/fitnesse.css", "body{color: red;}");
    request.setResource("files/fitnesse.css");
    response = responder.makeResponse(context, request);
    assertEquals("text/css", response.getContentType());
  }

  @Test
  public void testNavigationBackToFrontPage() throws Exception {
    request.setResource("files/");
    response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertSubString("href=\"/FrontPage\"", sender.sentData());
  }

  @Test
  public void shouldTellIfFileIsInFilesFolder() throws IOException {
    File rootPath = new File("./FitNesseRoot");
    assertTrue(isInFilesDirectory(rootPath, new File(rootPath, "files")));
    assertTrue(isInFilesDirectory(rootPath, new File(rootPath, "files/subfile")));
    assertFalse(isInFilesDirectory(rootPath, new File(rootPath, "files/..")));
    assertFalse(isInFilesDirectory(rootPath, new File("files")));
  }

  @Test
  public void shouldNotDealWithDirectoryOutsideFilesFolder() throws Exception {
    request.setResource("/files/../../");
    Response response = responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldNotDealWithEscapedDirectoryOutsideFilesFolder() throws Exception {
    request.setResource("/files/..%2f/");
    Response response = responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
  }

}
