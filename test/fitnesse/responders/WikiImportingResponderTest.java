// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockChunkedDataProvider;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

public class WikiImportingResponderTest {
  private WikiImportingResponder responder;
  private String baseUrl;
  private WikiImporterTest testData;
  private WikiImporter importer;

  @Before
  public void setUp() throws Exception {
    testData = new WikiImporterTest();
    testData.createRemoteRoot();
    testData.createLocalRoot();

    FitNesseUtil.startFitnesseWithContext(testData.remoteContext);
    baseUrl = "http://localhost:" + FitNesseUtil.PORT + "/";

    createResponder();
  }

  private void createResponder() throws Exception {
    importer = new WikiImporter();
    importer.setDeleteOrphanOption(false);
    responder = new WikiImportingResponder(importer);
    responder.path = new WikiPagePath();
    ChunkedResponse response = new ChunkedResponse("html", new MockChunkedDataProvider());
    response.sendTo(new MockResponseSender());
    responder.setResponse(response);
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  @Test
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

  @Test
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

  @Test
  public void testImportPropertiesGetAdded() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    checkProperties(testData.pageTwo, baseUrl, true, null);

    WikiPage importedPageOne = testData.pageTwo.getChildPage("PageOne");
    WikiPage rootPage = testData.remoteContext.getRootPage();
    checkProperties(importedPageOne, baseUrl + "PageOne", false, rootPage.getChildPage("PageOne"));

    WikiPage importedPageTwo = testData.pageTwo.getChildPage("PageTwo");
    checkProperties(importedPageTwo, baseUrl + "PageTwo", false, rootPage.getChildPage("PageTwo"));

    WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
    checkProperties(importedChildOne, baseUrl + "PageOne.ChildOne", false, rootPage.getChildPage("PageOne").getChildPage("ChildOne"));
  }

  private void checkProperties(WikiPage page, String source, boolean isRoot, WikiPage remotePage) throws Exception {
    WikiPageProperty props = page.getData().getProperties();
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

  private String simulateWebRequest(MockRequest request) throws Exception {
    ChunkedResponse response = getResponse(request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    return content;
  }

  @Test
  public void testHtmlOfMakeResponse() throws Exception {
    Response response = makeSampleResponse(baseUrl);
    MockResponseSender sender = new MockResponseSender();
    ((ChunkedResponse) response).turnOffChunking();
    sender.doSending(response);
    String content = sender.sentData();

    assertSubString("<html>", content);
    assertSubString("Wiki Import", content);

    assertSubString("PageTwo", content);
    assertSubString("PageTwo.PageOne", content);
    assertSubString("PageTwo.PageOne.ChildOne", content);
    assertSubString("Import complete.", content);
    assertSubString("3 pages were imported.", content);
  }

  @Test
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

    assertSubString("PageTwo", content);
    assertNotSubString("PageTwo.PageOne", content);
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
    ChunkedResponse response = (ChunkedResponse) responder.makeResponse(testData.localContext, request);
    response.turnOffChunking();
    return response;
  }

  private MockRequest makeRequest(String remoteUrl) {
    MockRequest request = new MockRequest();
    request.setResource("PageTwo");
    request.addInput("responder", "import");
    request.addInput("remoteUrl", remoteUrl);
    return request;
  }

  @Test
  public void testMakeResponseImportingNonRootPage() throws Exception {
    MockRequest request = makeRequest(baseUrl + "PageOne");

    Response response = responder.makeResponse(testData.localContext, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertNotNull(testData.pageTwo.getChildPage("ChildOne"));
    assertSubString("PageTwo.ChildOne", content);
    assertSubString("ChildOne", content);
  }

  @Test
  public void testRemoteUrlNotFound() throws Exception {
    String remoteUrl = baseUrl + "PageDoesntExist";
    Response response = makeSampleResponse(remoteUrl);

    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
  }

  @Test
  public void testErrorMessageForBadUrlProvided() throws Exception {
    String remoteUrl = baseUrl + "+blah";
    Response response = makeSampleResponse(remoteUrl);

    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();
    assertSubString("The URL's resource path, +blah, is not a valid WikiWord.", content);
  }

  @Test
  public void testListOfOrphanedPages() throws Exception {

    MockRequest request = makeRequest(baseUrl);
    String content = simulateWebRequest(request);

    assertNotSubString("orphan", content);
    //assertNotSubString("PageOne", content);
    //assertNotSubString("PageOne.ChildPagae", content);

    importer.getOrphans().add(new WikiPagePath(testData.pageOne));
    importer.getOrphans().add(new WikiPagePath(testData.childPageOne));

    content = simulateWebRequest(request);

    assertSubString("2 orphaned pages were found and have been removed.", content);
    assertSubString("PageOne", content);
    assertSubString("PageOne.ChildOne", content);
  }

  @Test
  public void testAutoUpdatingTurnedOn() throws Exception {
    MockRequest request = makeRequest(baseUrl);
    responder.setRequest(request);
    responder.page = new WikiPageDummy();
    responder.data = responder.page.getData();

    responder.initializeImporter();
    assertFalse(importer.getAutoUpdateSetting());

    request.addInput("autoUpdate", "1");
    responder.initializeImporter();
    assertTrue(importer.getAutoUpdateSetting());
  }

  @Test
  public void testAutoUpdateSettingDisplayed() throws Exception {

    MockRequest request = makeRequest(baseUrl);
    request.addInput("autoUpdate", "true");
    String content = simulateWebRequest(request);

    assertSubString("Automatic Update turned ON", content);

    request = makeRequest(baseUrl);
    content = simulateWebRequest(request);

    assertSubString("Automatic Update turned OFF", content);
  }

  // Tests for the rendering of import specific page details
  private WikiPage root;
  private WikiPage page;

  public void pageRenderingSetUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
  }

  @Test
  public void testImportedPageIndication() throws Exception {
    pageRenderingSetUp();

    page = WikiPageUtil.addPage(root, PathParser.parse("SamplePage"));
    PageData data = page.getData();
    WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);

    String content = getContentAfterSpecialImportHandling();

    assertSubString("<body class=\"imported\">", content);
  }

  @Test
  public void testEditActions() throws Exception {
    pageRenderingSetUp();

    page = WikiPageUtil.addPage(root, PathParser.parse("SamplePage"));
    PageData data = page.getData();
    page.commit(data);
    String content = getContentAfterSpecialImportHandling();

    assertNotSubString("Edit Locally", content);
    assertNotSubString("Edit Remotely", content);

    WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);
    content = getContentAfterSpecialImportHandling();

    assertTrue(WikiImportProperty.isImportedSubWiki(data));
    assertSubString("<a class=\"nav-link text-secondary\" href=\"SamplePage?edit\" accesskey=\"e\">Edit Locally</a>", content);
    assertSubString("<a class=\"nav-link text-secondary\" href=\"blah?responder=edit&amp;redirectToReferer=true&amp;redirectAction=importAndView\">Edit Remotely</a>", content);
  }

  private String getContentAfterSpecialImportHandling() {
    HtmlPage html = new PageFactory(FitNesseUtil.makeTestContext()).newPage();
    WikiImportingResponder.handleImportProperties(html, page);
    html.setNavTemplate("wikiNav.vm");
    html.put("actions", new WikiPageActions(page));
    return html.html(null);
  }

}
