// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.Responder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

public class RenamePageResponderTest extends ResponderTestCase {
  private WikiPagePath pageOnePath;
  private WikiPagePath pageTwoPath;
  private String pageOneName;
  private String pageTwoName;

  protected Responder responderInstance() {
    return new RenamePageResponder();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    pageOneName = "PageOne";
    pageTwoName = "PageTwo";
    pageOnePath = PathParser.parse(pageOneName);
    pageTwoPath = PathParser.parse(pageTwoName);
  }

  @Test
  public void testInvalidName() throws Exception {
    String invalidName = "FirstName.SecondName";
    String pageName = "MyPage";
    WikiPageUtil.addPage(root, PathParser.parse(pageName), "content");
    Response response = doRename(pageName, invalidName, true);

    assertHasRegexp("Cannot rename", getResponseContent(response));
  }

  @Test
  public void testDontRenameFrontPage() throws Exception {
    String frontPageName = "FrontPage";
    WikiPageUtil.addPage(root, PathParser.parse(frontPageName), "Content");
    Response response = doRename(frontPageName, "ReNamed", true);
    assertNotNull(response);
    assertSubString("Cannot rename", getResponseContent(response));
  }

  @Test
  public void testPageRedirection() throws Exception {
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("OneOne"), "Content");
    WikiPageUtil.addPage(pageOne, PathParser.parse("TwoOne"), "");
    Response response = doRename("OneOne.TwoOne", "ReName", true);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    assertEquals("/OneOne.ReName", response.getHeader("Location"));
  }

  @Test
  public void testPageWasRenamed() throws Exception {
    String originalName = "OneOne";
    WikiPagePath originalPath = PathParser.parse(originalName);
    String renamedName = "WonWon";
    WikiPagePath renamedPath = PathParser.parse(renamedName);

    PageCrawler crawler = root.getPageCrawler();
    WikiPageUtil.addPage(root, originalPath, "Content");
    assertTrue(crawler.pageExists(originalPath));
    assertFalse(crawler.pageExists(renamedPath));

    doRename(originalName, renamedName, true);

    assertTrue(crawler.pageExists(renamedPath));
    assertFalse(crawler.pageExists(originalPath));
  }

  @Test
  public void testReferencesChanged() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "Line one\nPageTwo\nLine three");
    WikiPageUtil.addPage(root, pageTwoPath, "Page two content");

    doRename(pageTwoName, "PageThree", true);
    WikiPage pageOne = root.getChildPage(pageOneName);
    assertEquals("Line one\nPageThree\nLine three", pageOne.getData().getContent());
  }

  @Test
  public void testBackSearchReferencesChanged() throws Exception {
    WikiPage topPage = WikiPageUtil.addPage(root, PathParser.parse("TopPage"), "");
    WikiPage pageOne = WikiPageUtil.addPage(topPage, pageOnePath, "Line one\n<TopPage.PageTwo\nLine three");
    WikiPageUtil.addPage(topPage, pageTwoPath, "Page two content");

    doRename("TopPage.PageTwo", "PageThree", true);
    assertEquals("Line one\n<TopPage.PageThree\nLine three", pageOne.getData().getContent());
  }

  @Test
  public void testReferencesNotChangedWhenDisabled() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "Line one\nPageTwo\nLine three");
    WikiPageUtil.addPage(root, pageTwoPath, "Page two content");

    doRename(pageTwoName, "PageThree", false);
    WikiPage pageOne = root.getChildPage(pageOneName);
    assertEquals("Line one\nPageTwo\nLine three", pageOne.getData().getContent());
  }

  @Test
  public void testDontRenameToExistingPage() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "Page one content");
    WikiPageUtil.addPage(root, pageTwoPath, "Page two content");

    Response response = doRename(pageOneName, pageTwoName, true);
    PageCrawler crawler = root.getPageCrawler();
    assertTrue(crawler.pageExists(pageOnePath));
    assertTrue(crawler.pageExists(pageTwoPath));
    assertEquals("Page two content", root.getChildPage(pageTwoName).getData().getContent());
    assertSubString("Cannot rename", getResponseContent(response));
  }

  @Test
  public void testChildPagesStayIntactWhenParentIsRenamed() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "page one");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildPage"), "child page");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildPage.GrandChild"), "grand child");

    doRename(pageOneName, pageTwoName, true);

    WikiPagePath path = PathParser.parse("PageTwo.ChildPage");
    PageCrawler crawler = root.getPageCrawler();
    assertTrue(crawler.pageExists(path));
    WikiPage page = crawler.getPage(path);
    assertNotNull(page);
    assertEquals("child page", page.getData().getContent());

    WikiPagePath grandChildPath = PathParser.parse("PageTwo.ChildPage.GrandChild");
    assertTrue(crawler.pageExists(grandChildPath));
    page = crawler.getPage(grandChildPath);
    assertNotNull(page);
    assertEquals("grand child", page.getData().getContent());
  }

  private String getResponseContent(Response response) throws Exception {
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

  private Response doRename(String fromName, String toName, boolean renameReferences) throws Exception {
    request.setResource(fromName);
    request.addInput("newName", toName);
    if (renameReferences)
      request.addInput("refactorReferences", "on");
    return responder.makeResponse(context, request);
  }
}
