// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.SampleFileUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DirectoryResponderTest {
  MockRequest request;
  private SimpleResponse response;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
    SampleFileUtility.makeSampleFiles(context.getRootPagePath());
  }

  @After
  public void tearDown() throws Exception {
    SampleFileUtility.deleteSampleFiles(context.getRootPagePath());
  }

  @Test
  public void testDirectotyListing() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = new FileResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("testDir", response.getContent());
    assertHasRegexp("testFile2", response.getContent());
    assertHasRegexp("testFile3", response.getContent());
    assertHasRegexp("<a href=\"/", response.getContent());
  }

  @Test
  public void testButtons() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = new FileResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);

    assertHasRegexp("Upload", response.getContent());
    assertHasRegexp("Create", response.getContent());
  }

  @Test
  public void testHtml() throws Exception {
    request.setResource("files/testDir/");
    Responder responder = new FileResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("/files/", response.getContent());
  }

  @Test
  public void testRedirectForDirectory() throws Exception {
    request.setResource("files/testDir");
    Responder responder = new FileResponder();
    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/files/testDir/", response.getHeader("Location"));
  }

  @Test
  public void testRedirectForDirectoryWithQueryParameters() throws Exception {
    request.setResource("files/testDir");
    request.setQueryString("responder=files&format=json");
    Responder responder = new FileResponder();
    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/files/testDir/?responder=files&format=json", response.getHeader("Location"));
  }
}
