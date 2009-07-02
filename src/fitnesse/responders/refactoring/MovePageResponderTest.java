// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class MovePageResponderTest extends ResponderTestCase {
  private WikiPage pageOne;
  private WikiPage pageA;
  private WikiPage pageTwo;
  private MovePageResponder moveResponder;

  protected Responder responderInstance() {
    return new MovePageResponder();
  }

  public void setUp() throws Exception {
    super.setUp();
    moveResponder = (MovePageResponder) responder;
    pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "^PageA");
    pageA = crawler.addPage(pageOne, PathParser.parse("PageA"), "content");
    pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"));
  }

  public void testIsChildOf() throws Exception {
    WikiPage parent = crawler.addPage(root, PathParser.parse("TheParent"));
    WikiPage child = crawler.addPage(parent, PathParser.parse("TheChild"));
    WikiPage grandChild = crawler.addPage(child, PathParser.parse("TheGrandChild"));
    assertTrue(moveResponder.pageIsAncestorOfNewParent(crawler.getFullPath(parent), crawler.getFullPath(child)));
    assertTrue(moveResponder.pageIsAncestorOfNewParent(crawler.getFullPath(parent), crawler.getFullPath(grandChild)));
    assertFalse(moveResponder.pageIsAncestorOfNewParent(crawler.getFullPath(child), crawler.getFullPath(parent)));
    assertFalse(moveResponder.pageIsAncestorOfNewParent(crawler.getFullPath(grandChild), crawler.getFullPath(parent)));
    assertTrue(moveResponder.pageIsAncestorOfNewParent(crawler.getFullPath(parent), crawler.getFullPath(parent)));
  }

  public void testMovePage() throws Exception {
    PageData data = pageA.getData();
    data.setAttribute("someAttribute", "someValue");
    pageA.commit(data);

    final String sourcePage = "PageOne.PageA";
    final String destinationPage = "PageTwo.PageA";
    final String destinationParent = "PageTwo";

    assertTrue(crawler.pageExists(root, PathParser.parse(sourcePage)));
    assertFalse(crawler.pageExists(root, PathParser.parse(destinationPage)));

    movePage(sourcePage, destinationParent, true);
    assertTrue(crawler.pageExists(root, PathParser.parse(destinationPage)));
    assertFalse(crawler.pageExists(root, PathParser.parse(sourcePage)));

    WikiPagePath destinationPath = PathParser.parse(destinationPage);
    WikiPage movedPage = crawler.getPage(root, destinationPath);
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
    return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
  }

  private SimpleResponse movePage(WikiPagePath pageToMove, WikiPagePath newParent, boolean refactorReferences) throws Exception {
    return movePage(PathParser.render(pageToMove), PathParser.render(newParent), refactorReferences);
  }

  public void testReferencesChanged() throws Exception {
    movePage("PageOne.PageA", "PageTwo", true);
    pageOne = root.getChildPage("PageOne");
    assertEquals(".PageTwo.PageA", pageOne.getData().getContent());
  }

  public void testReferenceToSubPageChanged() throws Exception {
    crawler.addPage(root, PathParser.parse("ReferingPage"), "PageOne.PageA");
    movePage("PageOne", "PageTwo", true);
    WikiPage referingPage = root.getChildPage("ReferingPage");
    assertEquals(".PageTwo.PageOne.PageA", referingPage.getData().getContent());
  }

  public void testReferenceToSubPageNotChangedWhenDisabled() throws Exception {
    crawler.addPage(root, PathParser.parse("ReferingPage"), "PageOne.PageA");
    movePage("PageOne", "PageTwo", false);
    WikiPage referingPage = root.getChildPage("ReferingPage");
    assertEquals("PageOne.PageA", referingPage.getData().getContent());
  }

  public void testCantMoveToSelf() throws Exception {
    pageA.getData().setAttribute("someAttribute", "someValue");
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));
    SimpleResponse response = movePage("PageOne.PageA", "PageOne", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));
  }

  public void testCantReplaceExistingPage() throws Exception {
    crawler.addPage(pageTwo, PathParser.parse("PageA"), "someContent");
    pageA.getData().setAttribute("someAttribute", "someValue");
    assertTrue(crawler.pageExists(root, PathParser.parse("PageTwo.PageA")));
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));

    SimpleResponse response = movePage("PageOne.PageA", "PageTwo", true);
    assertSubString("Cannot move", response.getContent());
    assertEquals("someContent", pageTwo.getChildPage("PageA").getData().getContent());
    assertEquals("content", pageA.getData().getContent());
    assertTrue(crawler.pageExists(root, PathParser.parse("PageTwo.PageA")));
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));
  }

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

    crawler.addPage(root, sourceChildOnePath, "child1Content");
    crawler.addPage(root, sourceChildTwoPath, "child2Content");
    crawler.addPage(root, sourceGrandChildPath);

    movePage(parentToMove, destinationParent, true);
    WikiPage movedPage = crawler.getPage(root, destinationPagePath);
    assertFalse(crawler.pageExists(root, sourceChildOnePath));
    assertFalse(crawler.pageExists(root, sourceChildTwoPath));
    List<?> children = movedPage.getChildren();
    assertEquals(2, children.size());
    assertTrue(crawler.pageExists(root, destinationChildOnePath));
    assertTrue(crawler.pageExists(root, destinationChildTwoPath));
    assertTrue(crawler.pageExists(root, destinationGrandChildPath));
  }

  public void testCantMovePageBelowChild() throws Exception {
    SimpleResponse response = movePage("PageOne", "PageOne.PageA", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));

  }

  public void testMoveToRoot() throws Exception {
    WikiPagePath originalPath = PathParser.parse("PageOne.PageA");
    assertTrue(crawler.pageExists(root, originalPath));
    movePage(originalPath, PathParser.parse(""), true);
    WikiPage movedPage = root.getChildPage(pageA.getName());
    assertFalse(crawler.pageExists(root, originalPath));
    assertEquals("content", movedPage.getData().getContent());
    assertEquals(PathParser.parse("PageA"), crawler.getFullPath(movedPage));
    pageOne = root.getChildPage(pageOne.getName());
    assertEquals(".PageA", pageOne.getData().getContent());
  }

  public void testMoveFromRoot() throws Exception {
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne")));
    movePage("PageOne", "PageTwo", true);
    WikiPage movedPage = pageTwo.getChildPage("PageOne");
    assertFalse(crawler.pageExists(root, PathParser.parse("PageOne")));
    assertEquals(".PageTwo.PageOne.PageA", movedPage.getData().getContent());
    assertEquals("PageTwo.PageOne", PathParser.render(crawler.getFullPath(movedPage)));
  }

  public void testRedirection() throws Exception {
    String url = moveResponder.createRedirectionUrl(pageOne, pageA.getName());
    assertEquals("PageOne.PageA", url);

    url = moveResponder.createRedirectionUrl(root, pageA.getName());
    assertEquals("PageA", url);
  }

  public void testBadMoveLocationName() throws Exception {
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));
    SimpleResponse response = movePage("PageOne.PageA", "NoSuchPage", true);
    assertSubString("Cannot move", response.getContent());
    assertTrue(crawler.pageExists(root, PathParser.parse("PageOne.PageA")));
  }

  public void testMovePageIntoItselfIsNotAllowed() throws Exception {
    crawler.addPage(root, PathParser.parse("TestPage"));
    SimpleResponse response = movePage("TestPage", "TestPage", true);

    assertFalse(crawler.pageExists(root, PathParser.parse("TestPage.TestPage")));
    assertEquals(400, response.getStatus());
    assertSubString("Cannot move", response.getContent());
  }
}
