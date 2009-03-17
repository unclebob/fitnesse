// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertMatches;
import static util.RegexTestCase.assertSubString;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

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
    context = new FitNesseContext();
    context.rootPagePath = SampleFileUtility.base;
    SampleFileUtility.makeSampleFiles();
    response = null;
    saveLocale = Locale.getDefault();
  }

  @After
  public void tearDown() throws Exception {
    if (response != null) response.readyToSend(new MockResponseSender());
    SampleFileUtility.deleteSampleFiles();
    Locale.setDefault(saveLocale);
  }

  @Test
  public void testFileContent() throws Exception {
    request.setResource("files/testFile1");
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    response = responder.makeResponse(context, request);
    RegexTestCase.assertEquals(InputStreamResponse.class, response.getClass());
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertSubString("file1 content", sender.sentData());
  }

  @Test
  public void testSpacesInFileName() throws Exception {
    String restoredPath = FileResponder.restoreRealSpacesInFileName("files/test%20File%20With%20Spaces%20In%20Name");
    assertEquals("files/test File With Spaces In Name", restoredPath);

    request.setResource("files/file4%20with%20spaces.txt");
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    assertEquals("files/file4 with spaces.txt", responder.resource);
  }

  @Test
  public void testLastModifiedHeader() throws Exception {
    Locale.setDefault(Locale.US);
    request.setResource("files/testFile1");
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    response = responder.makeResponse(context, request);
    String lastModifiedHeader = response.getHeader("Last-Modified");
    assertMatches(HTTP_DATE_REGEXP, lastModifiedHeader);
  }

  @Test
  public void test304IfNotModified() throws Exception {
    Locale.setDefault(Locale.US);
    Calendar now = new GregorianCalendar();
    now.add(Calendar.DATE, -1);
    String yesterday = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());
    now.add(Calendar.DATE, 2);
    String tomorrow = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());

    request.setResource("files/testFile1");
    request.addHeader("If-Modified-Since", yesterday);
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    response = responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    request.setResource("files/testFile1");
    request.addHeader("If-Modified-Since", tomorrow);
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    SimpleResponse notModifiedResponse = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(304, notModifiedResponse.getStatus());
    assertEquals("", notModifiedResponse.getContent());
    assertMatches(HTTP_DATE_REGEXP, notModifiedResponse.getHeader("Date"));
    assertNotNull(notModifiedResponse.getHeader("Cache-Control"));
  }

  @Test
  public void testRecoverFromUnparseableDateInIfNotModifiedHeader() throws Exception {
    request.setResource("files/testFile1");
    request.addHeader("If-Modified-Since", "Unparseable Date");
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    response = responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testNotFoundFile() throws Exception {
    request.setResource("files/something/that/aint/there");
    Responder notFoundResponder = FileResponder.makeResponder(request, SampleFileUtility.base);
    SimpleResponse response = (SimpleResponse) notFoundResponder.makeResponse(context, request);
    assertEquals(404, response.getStatus());
    assertHasRegexp("files/something/that/aint/there", response.getContent());
  }

  @Test
  public void testCssMimeType() throws Exception {
    SampleFileUtility.addFile("/files/fitnesse.css", "body{color: red;}");
    request.setResource("files/fitnesse.css");
    responder = (FileResponder) FileResponder.makeResponder(request, SampleFileUtility.base);
    response = responder.makeResponse(context, request);
    assertEquals("text/css", response.getContentType());
  }
}
