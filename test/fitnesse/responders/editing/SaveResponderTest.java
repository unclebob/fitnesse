// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SaveResponderTest {
  private WikiPage root;
  private Response response;
  public MockRequest request;
  public SaveResponder responder;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitNesseUtil.makeTestContext(root);
    request = new MockRequest();
    responder = new SaveResponder();
    SaveRecorder.clear();
  }

  @Test
  public void testResponse() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage"));
    prepareRequest("ChildPage");

    Response response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: /ChildPage", response.makeHttpHeaders());

    String newContent = root.getChildPage("ChildPage").getData().getContent();
    assertEquals("some new content", newContent);

    checkRecentChanges(root, "ChildPage");
  }

  private void prepareRequest(String pageName) {
    request.setResource(pageName);
    request.addInput(EditResponder.TIME_STAMP, "12345");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    request.addInput(EditResponder.HELP_TEXT, "some help");
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());
  }

  @Test
  public void testResponseWithRedirect() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage"));
    prepareRequest("ChildPage");
    request.addInput("redirect", "http://fitnesse.org:8080/SomePage");

    Response response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals(303, response.getStatus());
    assertHasRegexp("Location: http://fitnesse.org:8080/SomePage", response.makeHttpHeaders());
  }

  private void checkRecentChanges(WikiPage source, String changedPage) throws Exception {
    assertTrue("RecentChanges should exist", source.hasChildPage("RecentChanges"));
    String recentChanges = source.getChildPage("RecentChanges").getData().getContent();
    assertTrue("ChildPage should be in RecentChanges", recentChanges.contains(changedPage));
  }

  @Test
  public void testCanCreatePage() throws Exception {
    prepareRequest("ChildPageTwo");

    responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    assertEquals(true, root.hasChildPage("ChildPageTwo"));
    String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
    assertEquals("some new content", newContent);
    assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
    checkRecentChanges(root, "ChildPageTwo");
  }

  @Test
  public void testCanCreatePageWithoutTicketIdAndEditTime() throws Exception {
    request.setResource("ChildPageTwo");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    request.addInput(EditResponder.HELP_TEXT, "some help");
    request.addInput(EditResponder.SUITES, "some help");

    responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    assertEquals(true, root.hasChildPage("ChildPageTwo"));
    String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
    assertEquals("some new content", newContent);
    assertEquals("some help", root.getChildPage("ChildPageTwo").getData().getAttribute("Help"));
    assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
    checkRecentChanges(root, "ChildPageTwo");
  }

  @Test
  public void testKnowsWhenToMerge() throws Exception {
    String simplePageName = "SimplePageName";
    createAndSaveANewPage(simplePageName);

    request.setResource(simplePageName);
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    request.addInput(EditResponder.TIME_STAMP, "" + (SaveRecorder.timeStamp() - 10000));
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    assertHasRegexp("Merge", response.getContent());
  }

  @Test
  public void testKnowWhenNotToMerge() throws Exception {
    String pageName = "NewPage";
    createAndSaveANewPage(pageName);
    String newContent = "some new Content work damn you!";
    request.setResource(pageName);
    request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent);
    request.addInput(EditResponder.TIME_STAMP, "" + SaveRecorder.timeStamp());
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

    Response response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals(303, response.getStatus());

    request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent + " Ok I'm working now");
    request.addInput(EditResponder.TIME_STAMP, "" + SaveRecorder.timeStamp());
    response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals(303, response.getStatus());
  }

  @Test
  public void testUsernameIsSavedInPageProperties() throws Exception {
    addRequestParameters();
    request.setCredentials("Aladdin", "open sesame");
    response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    String user = root.getChildPage("EditPage").getData().getAttribute(PageData.LAST_MODIFYING_USER);
    assertEquals("Aladdin", user);
  }

  private void createAndSaveANewPage(String pageName) throws Exception {
    WikiPage simplePage = WikiPageUtil.addPage(root, PathParser.parse(pageName));

    PageData data = simplePage.getData();
    SaveRecorder.pageSaved(data, 0);
    simplePage.commit(data);
  }

  private void doSimpleEdit() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("EditPage"));
    addRequestParameters();

    response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
  }

  private void addRequestParameters() {
    prepareRequest("EditPage");
  }

  @Test
  public void testHasVersionHeader() throws Exception {
    doSimpleEdit();
    assertTrue("header missing", response.getHeader("Current-Version") != null);
  }
}
