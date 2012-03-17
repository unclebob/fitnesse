// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;

public class DirectoryResponderTest extends RegexTestCase {
  MockRequest request;
  private SimpleResponse response;
  private FitNesseContext context;

  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext(null);
    context.rootPagePath = SampleFileUtility.base;
    SampleFileUtility.makeSampleFiles();
  }

  public void tearDown() throws Exception {
    SampleFileUtility.deleteSampleFiles();
  }

  public void testDirectotyListing() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("testDir", response.getContent());
    assertHasRegexp("testFile2", response.getContent());
    assertHasRegexp("testFile3", response.getContent());
    assertHasRegexp("<a href=\"/", response.getContent());
  }

  public void testButtons() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
    response = (SimpleResponse) responder.makeResponse(context, request);

    assertHasRegexp("Upload", response.getContent());
    assertHasRegexp("Create", response.getContent());
  }

  public void testHtml() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("/files/", response.getContent());
  }

  public void testRedirectForDirectory() throws Exception {
    request.setResource("files/testDir");
    Responder responder = FileResponder.makeResponder(request, SampleFileUtility.base);
    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/files/testDir/", response.getHeader("Location"));
  }
}
