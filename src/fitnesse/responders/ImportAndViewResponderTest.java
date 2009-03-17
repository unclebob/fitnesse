// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import junit.framework.TestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperties;

public class ImportAndViewResponderTest extends TestCase {
  private WikiImporterTest testData;
  private ImportAndViewResponder responder;

  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot();
    testData.createLocalRoot();

    FitNesseUtil.startFitnesse(testData.remoteRoot);

    responder = new ImportAndViewResponder();
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  public void testRedirect() throws Exception {
    Response response = getResponse();

    assertEquals(303, response.getStatus());
    assertEquals("PageTwo", response.getHeader("Location"));
  }

  private Response getResponse() throws Exception {
    FitNesseContext context = new FitNesseContext(testData.localRoot);
    MockRequest request = new MockRequest();
    request.setResource("PageTwo");
    return responder.makeResponse(context, request);
  }

  public void testPageContentIsUpdated() throws Exception {
    PageData data = testData.pageTwo.getData();
    WikiPageProperties props = data.getProperties();

    WikiImportProperty importProps = new WikiImportProperty("http://localhost:" + FitNesseUtil.port + "/PageTwo");
    importProps.addTo(props);
    testData.pageTwo.commit(data);

    getResponse();

    data = testData.pageTwo.getData();
    assertEquals("page two", data.getContent());
  }
}
