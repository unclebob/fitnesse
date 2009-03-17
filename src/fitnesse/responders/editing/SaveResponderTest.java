// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.SaveRecorder;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SaveResponderTest extends RegexTestCase {
  private WikiPage root;
  private Response response;
  public MockRequest request;
  public Responder responder;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new SaveResponder();
    SaveResponder.contentFilter = null;
  }

  protected void tearDown() throws Exception {
    SaveResponder.contentFilter = null;
  }

  public void testResponse() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"));
    prepareRequest("ChildPage");

    Response response = responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: ChildPage", response.makeHttpHeaders());

    String newContent = root.getChildPage("ChildPage").getData().getContent();
    assertEquals("some new content", newContent);

    checkRecentChanges(root, "ChildPage");
  }

  private void prepareRequest(String pageName) {
    request.setResource(pageName);
    request.addInput(EditResponder.SAVE_ID, "12345");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());
  }

  public void testResponseWithRedirect() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"));
    prepareRequest("ChildPage");
    request.addInput("redirect", "http://fitnesse.org:8080/SomePage");

    Response response = responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: http://fitnesse.org:8080/SomePage", response.makeHttpHeaders());
  }

  private void checkRecentChanges(WikiPage source, String changedPage) throws Exception {
    assertTrue("RecentChanges should exist", source.hasChildPage("RecentChanges"));
    String recentChanges = source.getChildPage("RecentChanges").getData().getContent();
    assertTrue("ChildPage should be in RecentChanges", recentChanges.indexOf(changedPage) != -1);
  }

  public void testCanCreatePage() throws Exception {
    prepareRequest("ChildPageTwo");

    responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(true, root.hasChildPage("ChildPageTwo"));
    String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
    assertEquals("some new content", newContent);
    assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
    checkRecentChanges(root, "ChildPageTwo");
  }

  public void testKnowsWhenToMerge() throws Exception {
    String simplePageName = "SimplePageName";
    createAndSaveANewPage(simplePageName);

    request.setResource(simplePageName);
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    request.addInput(EditResponder.SAVE_ID, "" + (SaveRecorder.newIdNumber() - 10000));
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

    assertHasRegexp("Merge", response.getContent());
  }

  public void testKnowWhenNotToMerge() throws Exception {
    String pageName = "NewPage";
    createAndSaveANewPage(pageName);
    String newContent = "some new Content work damn you!";
    request.setResource(pageName);
    request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent);
    request.addInput(EditResponder.SAVE_ID, "" + SaveRecorder.newIdNumber());
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

    Response response = responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(303, response.getStatus());

    request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent + " Ok I'm working now");
    request.addInput(EditResponder.SAVE_ID, "" + SaveRecorder.newIdNumber());
    response = responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(303, response.getStatus());
  }

  public void testUsernameIsSavedInPageProperties() throws Exception {
    addRequestParameters();
    request.setCredentials("Aladdin", "open sesame");
    response = responder.makeResponse(new FitNesseContext(root), request);

    String user = root.getChildPage("EditPage").getData().getAttribute(WikiPage.LAST_MODIFYING_USER);
    assertEquals("Aladdin", user);
  }

  public void testContentFilter() throws Exception {
    SaveResponder.contentFilter = new ContentFilter() {
      public boolean isContentAcceptable(String content, String page) {
        return false;
      }
    };
    crawler.addPage(root, PathParser.parse("ChildPage"));
    prepareRequest("ChildPage");

    Response response = responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(200, response.getStatus());
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertSubString("Your changes will not be saved!", sender.sentData());
  }

  private void createAndSaveANewPage(String pageName) throws Exception {
    WikiPage simplePage = crawler.addPage(root, PathParser.parse(pageName));

    PageData data = simplePage.getData();
    SaveRecorder.pageSaved(data);
    simplePage.commit(data);
  }

  private void doSimpleEdit() throws Exception {
    crawler.addPage(root, PathParser.parse("EditPage"));
    addRequestParameters();

    response = responder.makeResponse(new FitNesseContext(root), request);
  }

  private void addRequestParameters() {
    prepareRequest("EditPage");
  }

  public void testHasVersionHeader() throws Exception {
    doSimpleEdit();
    assertTrue("header missing", response.getHeader("Previous-Version") != null);
  }
}
