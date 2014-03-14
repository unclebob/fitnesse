// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPageProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImportAndViewResponderTest {
  private WikiImporterTest testData;
  private ImportAndViewResponder responder;

  @Before
  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot();
    testData.createLocalRoot();

    FitNesseUtil.startFitnesse(testData.remoteRoot);

    responder = new ImportAndViewResponder();
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  @Test
  public void testRedirect() throws Exception {
    Response response = getResponse();

    assertEquals(303, response.getStatus());
    assertEquals("/PageTwo", response.getHeader("Location"));
  }

  private Response getResponse() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext(testData.localRoot);
    MockRequest request = new MockRequest();
    request.setResource("PageTwo");
    return responder.makeResponse(context, request);
  }

  @Test
  public void testPageContentIsUpdated() throws Exception {
    PageData data = testData.pageTwo.getData();
    WikiPageProperties props = data.getProperties();

    WikiImportProperty importProps = new WikiImportProperty("http://localhost:" + FitNesseUtil.PORT + "/PageTwo");
    importProps.addTo(props);
    testData.pageTwo.commit(data);

    getResponse();

    data = testData.pageTwo.getData();
    assertEquals("page two", data.getContent());
  }
}
