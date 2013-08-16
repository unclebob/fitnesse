// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import java.util.List;

import fitnesse.Responder;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class MovePageResponderTest extends ResponderTestCase {
  private WikiPage pageOne;
  private WikiPage pageA;
  private WikiPage pageTwo;
  private MovePageResponder moveResponder;
  private PageCrawler crawler;

  protected Responder responderInstance() {
    return new MovePageResponder();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    moveResponder = (MovePageResponder) responder;
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "^PageA");
    pageA = WikiPageUtil.addPage(pageOne, PathParser.parse("PageA"), "content");
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"));
    crawler = root.getPageCrawler();
  }

  @Test
  public void testIsChildOf() throws Exception {
    WikiPage parent = WikiPageUtil.addPage(root, PathParser.parse("TheParent"));
    WikiPage child = WikiPageUtil.addPage(parent, PathParser.parse("TheChild"));
    WikiPage grandChild = WikiPageUtil.addPage(child, PathParser.parse("TheGrandChild"));

    assertIsAncestor(parent, child);
    assertIsAncestor(parent, grandChild);
    assertIsAncestor(parent, parent);

    assertIsNotAncestor(child, parent);
    assertIsNotAncestor(grandChild, parent);
  }

  private void assertIsNotAncestor(WikiPage supposedAncestor, WikiPage supposedDescendent) {
    assertFalse(isAncestor(supposedAncestor, supposedDescendent));
  }

  private void assertIsAncestor(WikiPage expectedAncestor, WikiPage expectedDescendent) {
    assertTrue(isAncestor(expectedAncestor, expectedDescendent));
  }

  private boolean isAncestor(WikiPage ancestor, WikiPage descendent) {
    return moveResponder.pageIsAncestorOfNewParent(ancestor.getPageCrawler().getFullPath(),
            descendent.getPageCrawler().getFullPath());
  }

  @Test
  public void testMovePage() throws Exception {
    PageData data = pageA.getData();
    data.setAttribute("someAttribute", "someValue");
    pageA.commit(data);

    final String sourcePage = "PageOne.PageA";
    final String destinationPage = "PageTwo.PageA";
    final String destinationParent = "PageTwo";

    assertTrue(crawler.pageExists(PathParser.parse(sourcePage)));
    assertFalse(crawler.pageExists(PathParser.parse(destinationPage)));

    movePage(sourcePage, destinationParent, true);
    assertTrue(crawler.pageExists(PathParser.parse(destinationPage)));
    assertFalse(crawler.pageExists(PathParser.parse(sourcePage)));

    WikiPagePath destinationPath = PathParser.parse(destinationPage);
    WikiPage movedPage = crawler.getPage(destinationPath);
    data = movedPage.getData();
    assertEquals("content", data.getContent());
    assertEquals("someValue", data.getAttribute("someAttribute"));
  }

  private SimpleResponse movePage(String pageToMove, String newParent, boolean refactorReferences) throws Exception  //todo RCM 2/8/05 Change all callers to use wikiPagePath version below.
  {
    request.addInput("newLocation", newParent);
    request.setResource(pageToMove);
    if (refactorReferences)
      request.addInput("refactorReferences", "on");
    return (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
  }

  private SimpleResponse movePage(WikiPagePath pageToMove, WikiPagePath newParent, boolean refactorReferences) throws Exception {
    return movePage(PathParser.render(pageToMove), PathParser.render(newParent), refactorReferences);
  }

  @Test
  public void testReferencesChanged() throws Exception {
    movePage("PageOne.PageA", "PageTwo", true);
    pageOne = root.getChildPage("PageOne");
    assertEquals(".PageTwo.PageA", pageOne.getData().getContent());
  }

  @Test
  public void testReferenceToSubPageChanged() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("ReferingPage"), "PageOne.PageA");
    movePage("PageOne", "PageTwo", true);
    WikiPage referingPage = root.getChildPage("ReferingPage");
    assertEquals(".PageTwo.PageOne.PageA", referingPage.getData().getContent());
  }

  @Test
  public void testReferenceToSubPageNotChangedWhenDisabled() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("ReferingPage"), "PageOne.PageA");
    movePage("PageOne", "PageTwo", false);
    WikiPage referingPage = root.getChildPage("ReferingPage");
    assertEquals("PageOne.PageA", referingPage.getData().getContent());
  }

  @Test
  public void testCantMoveToSelf() throws Exception {
    pageA.getData().setAttribute("someAttribute", "someValue");
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
    SimpleResponse response = movePage("PageOne.PageA", "PageOne", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
  }

  @Test
  public void testCantReplaceExistingPage() throws Exception {
    WikiPageUtil.addPage(pageTwo, PathParser.parse("PageA"), "someContent");
    pageA.getData().setAttribute("someAttribute", "someValue");
    assertTrue(crawler.pageExists(PathParser.parse("PageTwo.PageA")));
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));

    SimpleResponse response = movePage("PageOne.PageA", "PageTwo", true);
    assertSubString("Cannot move", response.getContent());
    assertEquals("someContent", pageTwo.getChildPage("PageA").getData().getContent());
    assertEquals("content", pageA.getData().getContent());
    assertTrue(crawler.pageExists(PathParser.parse("PageTwo.PageA")));
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
  }

  @Test
  public void testChildrenGetMovedIfParentMoves() throws Exception {
    final String sourceChildOne = "PageOne.PageA.ChildOne";
    final String sourceChildTwo = "PageOne.PageA.ChildTwo";
    final String sourceGrandChild = "PageOne.PageA.ChildTwo.ChildTwoDotOne";
    final String parentToMove = "PageOne.PageA";
    final String destinationParent = "PageTwo";
    final String destinationPage = "PageTwo.PageA";
    final String destinationChildOne = "PageTwo.PageA.ChildOne";
    final String destinationChildTwo = "PageTwo.PageA.ChildTwo";
    final String destinationGrandChild = "PageTwo.PageA.ChildTwo.ChildTwoDotOne";

    WikiPagePath sourceChildOnePath = PathParser.parse(sourceChildOne);
    WikiPagePath sourceChildTwoPath = PathParser.parse(sourceChildTwo);
    WikiPagePath sourceGrandChildPath = PathParser.parse(sourceGrandChild);
    WikiPagePath destinationPagePath = PathParser.parse(destinationPage);
    WikiPagePath destinationChildOnePath = PathParser.parse(destinationChildOne);
    WikiPagePath destinationChildTwoPath = PathParser.parse(destinationChildTwo);
    WikiPagePath destinationGrandChildPath = PathParser.parse(destinationGrandChild);

    WikiPageUtil.addPage(root, sourceChildOnePath, "child1Content");
    WikiPageUtil.addPage(root, sourceChildTwoPath, "child2Content");
    WikiPageUtil.addPage(root, sourceGrandChildPath);

    movePage(parentToMove, destinationParent, true);
    WikiPage movedPage = crawler.getPage(destinationPagePath);
    assertFalse(crawler.pageExists(sourceChildOnePath));
    assertFalse(crawler.pageExists(sourceChildTwoPath));
    List<?> children = movedPage.getChildren();
    assertEquals(2, children.size());
    assertTrue(crawler.pageExists(destinationChildOnePath));
    assertTrue(crawler.pageExists(destinationChildTwoPath));
    assertTrue(crawler.pageExists(destinationGrandChildPath));
  }

  @Test
  public void testCantMovePageBelowChild() throws Exception {
    SimpleResponse response = movePage("PageOne", "PageOne.PageA", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
  }

  @Test
  public void testMoveToRoot() throws Exception {
    WikiPagePath originalPath = PathParser.parse("PageOne.PageA");
    assertTrue(crawler.pageExists(originalPath));
    movePage(originalPath, PathParser.parse(""), true);
    WikiPage movedPage = root.getChildPage(pageA.getName());
    assertFalse(crawler.pageExists(originalPath));
    assertEquals("content", movedPage.getData().getContent());
    assertEquals(PathParser.parse("PageA"), movedPage.getPageCrawler().getFullPath());
    pageOne = root.getChildPage(pageOne.getName());
    assertEquals(".PageA", pageOne.getData().getContent());
  }

  @Test
  public void testMoveFromRoot() throws Exception {
    assertTrue(crawler.pageExists(PathParser.parse("PageOne")));
    movePage("PageOne", "PageTwo", true);
    WikiPage movedPage = pageTwo.getChildPage("PageOne");
    assertFalse(crawler.pageExists(PathParser.parse("PageOne")));
    assertEquals(".PageTwo.PageOne.PageA", movedPage.getData().getContent());
    assertEquals("PageTwo.PageOne", PathParser.render(movedPage.getPageCrawler().getFullPath()));
  }

  @Test
  public void testRedirection() throws Exception {
    String url = moveResponder.createRedirectionUrl(pageOne, pageA.getName());
    assertEquals("PageOne.PageA", url);

    url = moveResponder.createRedirectionUrl(root, pageA.getName());
    assertEquals("PageA", url);
  }

  @Test
  public void testBadMoveLocationName() throws Exception {
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
    SimpleResponse response = movePage("PageOne.PageA", "NoSuchPage", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(PathParser.parse("PageOne.PageA")));
  }

  @Test
  public void testMovePageIntoItselfIsNotAllowed() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
    SimpleResponse response = movePage("TestPage", "TestPage", true);

    assertFalse(crawler.pageExists(PathParser.parse("TestPage.TestPage")));
    assertEquals(400, response.getStatus());
    assertSubString("Cannot move", response.getContent());
  }
}
