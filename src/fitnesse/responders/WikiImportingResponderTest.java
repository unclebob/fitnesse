// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;

public class WikiImportingResponderTest extends RegexTestCase {
  private WikiImportingResponder responder;
  private String baseUrl;
  private WikiImporterTest testData;

  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot();
    testData.createLocalRoot();

    FitNesseUtil.startFitnesse(testData.remoteRoot);
    baseUrl = "http://localhost:" + FitNesseUtil.port + "/";

    createResponder();
  }

  private void createResponder() throws Exception {
    responder = new WikiImportingResponder();
    responder.path = new WikiPagePath();
    ChunkedResponse response = new ChunkedResponse("html");
    response.readyToSend(new MockResponseSender());
    responder.setResponse(response);
    responder.getImporter().setDeleteOrphanOption(false);
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  public void testActionsOfMakeResponse() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    assertEquals(2, testData.pageTwo.getChildren().size());
    WikiPage importedPageOne = testData.pageTwo.getChildPage("PageOne");
    assertNotNull(importedPageOne);
    assertEquals("page one", importedPageOne.getData().getContent());

    WikiPage importedPageTwo = testData.pageTwo.getChildPage("PageTwo");
    assertNotNull(importedPageTwo);
    assertEquals("page two", importedPageTwo.getData().getContent());

    assertEquals(1, importedPageOne.getChildren().size());
    WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
    assertNotNull(importedChildOne);
    assertEquals("child one", importedChildOne.getData().getContent());
  }

  public void testImportingFromNonRootPageUpdatesPageContent() throws Exception {
    PageData data = testData.pageTwo.getData();
    WikiImportProperty importProperty = new WikiImportProperty(baseUrl + "PageOne");
    importProperty.addTo(data.getProperties());
    data.setContent("nonsense");
    testData.pageTwo.commit(data);

    Response response = makeSampleResponse("blah");
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    data = testData.pageTwo.getData();
    assertEquals("page one", data.getContent());

    assertFalse(WikiImportProperty.createFrom(data.getProperties()).isRoot());
  }

  public void testImportPropertiesGetAdded() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    checkProperties(testData.pageTwo, baseUrl, true, null);

    WikiPage importedPageOne = testData.pageTwo.getChildPage("PageOne");
    checkProperties(importedPageOne, baseUrl + "PageOne", false, testData.remoteRoot.getChildPage("PageOne"));

    WikiPage importedPageTwo = testData.pageTwo.getChildPage("PageTwo");
    checkProperties(importedPageTwo, baseUrl + "PageTwo", false, testData.remoteRoot.getChildPage("PageTwo"));

    WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
    checkProperties(importedChildOne, baseUrl + "PageOne.ChildOne", false, testData.remoteRoot.getChildPage("PageOne").getChildPage("ChildOne"));
  }

  private void checkProperties(WikiPage page, String source, boolean isRoot, WikiPage remotePage) throws Exception {
    WikiPageProperties props = page.getData().getProperties();
    if (!isRoot)
      assertFalse("should not have Edit property", props.has("Edit"));

    WikiImportProperty importProperty = WikiImportProperty.createFrom(props);
    assertNotNull(importProperty);
    assertEquals(source, importProperty.getSourceUrl());
    assertEquals(isRoot, importProperty.isRoot());

    if (remotePage != null) {
      long remoteLastModificationTime = remotePage.getData().getProperties().getLastModificationTime().getTime();
      long importPropertyLastModificationTime = importProperty.getLastRemoteModificationTime().getTime();
      assertEquals(remoteLastModificationTime, importPropertyLastModificationTime);
    }
  }

  public void testHtmlOfMakeResponse() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertSubString("<html>", content);
    assertSubString("Wiki Import", content);

    assertSubString("href=\"PageTwo\"", content);
    assertSubString("href=\"PageTwo.PageOne\"", content);
    assertSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
    assertSubString("href=\"PageTwo.PageTwo\"", content);
    assertSubString("Import complete.", content);
    assertSubString("3 pages were imported.", content);
  }

  public void testHtmlOfMakeResponseWithNoModifications() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    // import a second time... nothing was modified
    createResponder();
    response = makeSampleResponse(baseUrl);
    sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertSubString("<html>", content);
    assertSubString("Wiki Import", content);

    assertSubString("href=\"PageTwo\"", content);
    assertNotSubString("href=\"PageTwo.PageOne\"", content);
    assertNotSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
    assertNotSubString("href=\"PageTwo.PageTwo\"", content);
    assertSubString("Import complete.", content);
    assertSubString("0 pages were imported.", content);
    assertSubString("3 pages were unmodified.", content);
  }

  private ChunkedResponse makeSampleResponse(String remoteUrl) throws Exception {
    MockRequest request = makeRequest(remoteUrl);

    return getResponse(request);
  }

  private ChunkedResponse getResponse(MockRequest request) throws Exception {
    Response response = responder.makeResponse(new FitNesseContext(testData.localRoot), request);
    assertTrue(response instanceof ChunkedResponse);
    return (ChunkedResponse) response;
  }

  private MockRequest makeRequest(String remoteUrl) {
    MockRequest request = new MockRequest();
    request.setResource("PageTwo");
    request.addInput("responder", "import");
    request.addInput("remoteUrl", remoteUrl);
    return request;
  }

  public void testMakeResponseImportingNonRootPage() throws Exception {
    MockRequest request = makeRequest(baseUrl + "PageOne");

    Response response = responder.makeResponse(new FitNesseContext(testData.localRoot), request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertNotNull(testData.pageTwo.getChildPage("ChildOne"));
    assertSubString("href=\"PageTwo.ChildOne\"", content);
    assertSubString(">ChildOne<", content);
  }

  public void testRemoteUrlNotFound() throws Exception {
    String remoteUrl = baseUrl + "PageDoesntExist";
    Response response = makeSampleResponse(remoteUrl);

    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
  }

  public void testErrorMessageForBadUrlProvided() throws Exception {
    String remoteUrl = baseUrl + "blah";
    Response response = makeSampleResponse(remoteUrl);

    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The URL's resource path, blah, is not a valid WikiWord.", content);
  }

  public void testUnauthorizedResponse() throws Exception {
    makeSecurePage(testData.remoteRoot);

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    checkRemoteLoginForm(content);
  }

  private void makeSecurePage(WikiPage page) throws Exception {
    PageData data = page.getData();
    data.setAttribute(WikiPage.SECURE_READ);
    page.commit(data);
    FitNesseUtil.context.authenticator = new OneUserAuthenticator("joe", "blow");
  }

  private void checkRemoteLoginForm(String content) {
    assertHasRegexp("The wiki at .* requires authentication.", content);
    assertSubString("<form", content);
    assertHasRegexp("<input[^>]*name=\"remoteUsername\"", content);
    assertHasRegexp("<input[^>]*name=\"remotePassword\"", content);
  }

  public void testUnauthorizedResponseFromNonRoot() throws Exception {
    WikiPage childPage = testData.remoteRoot.getChildPage("PageOne");
    makeSecurePage(childPage);

    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The wiki at " + baseUrl + "PageOne requires authentication.", content);
    assertSubString("<form", content);
  }

  public void testImportingFromSecurePageWithCredentials() throws Exception {
    makeSecurePage(testData.remoteRoot);

    MockRequest request = makeRequest(baseUrl);
    request.addInput("remoteUsername", "joe");
    request.addInput("remotePassword", "blow");
    Response response = getResponse(request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertNotSubString("requires authentication", content);
    assertSubString("3 pages were imported.", content);

    assertEquals("joe", WikiImporter.remoteUsername);
    assertEquals("blow", WikiImporter.remotePassword);
  }

  public void testListOfOrphanedPages() throws Exception {
    WikiImporter importer = new WikiImporter();

    String tail = responder.makeTailHtml(importer).html();

    assertNotSubString("orphan", tail);
    assertNotSubString("PageOne", tail);
    assertNotSubString("PageOne.ChildPagae", tail);

    importer.getOrphans().add(new WikiPagePath(testData.pageOne));
    importer.getOrphans().add(new WikiPagePath(testData.childPageOne));

    tail = responder.makeTailHtml(importer).html();

    assertSubString("2 orphaned pages were found and have been removed.", tail);
    assertSubString("PageOne", tail);
    assertSubString("PageOne.ChildOne", tail);
  }

  public void testAutoUpdatingTurnedOn() throws Exception {
    MockRequest request = makeRequest(baseUrl);
    responder.setRequest(request);
    responder.data = new PageData(new WikiPageDummy());

    responder.initializeImporter();
    assertFalse(responder.getImporter().getAutoUpdateSetting());

    request.addInput("autoUpdate", "1");
    responder.initializeImporter();
    assertTrue(responder.getImporter().getAutoUpdateSetting());
  }

  public void testAutoUpdateSettingDisplayedInTail() throws Exception {
    WikiImporter importer = new MockWikiImporter();
    importer.setAutoUpdateSetting(true);

    String tail = responder.makeTailHtml(importer).html();
    assertSubString("Automatic Update turned ON", tail);

    importer.setAutoUpdateSetting(false);

    tail = responder.makeTailHtml(importer).html();
    assertSubString("Automatic Update turned OFF", tail);
  }
}
