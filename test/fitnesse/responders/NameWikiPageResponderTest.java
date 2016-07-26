// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;

import java.util.HashSet;
import java.util.Set;

public class NameWikiPageResponderTest {
  private FitNesseContext context;
  private WikiPage root;
  private NameWikiPageResponder responder;
  private MockRequest request;
  private String frontPageName;
  private String pageOneName;
  private String pageTwoName;
  private String pageThreeName;
  private String pageFourName;
  private String pageFiveName;
  private WikiPagePath frontPagePath;
  private WikiPagePath pageOnePath;
  private WikiPagePath pageTwoPath;
  private WikiPagePath pageThreePath;
  private WikiPagePath pageFourPath;
  private WikiPagePath pageFivePath;
  private String helloTag;
  private String worldTag;
  private String fitnesseTag;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    responder = new NameWikiPageResponder();
    request = new MockRequest();

    frontPageName = "FrontPage";
    pageOneName   = "PageOne";
    pageTwoName   = "PageTwo";
    pageThreeName = "PageThree";
    pageFourName  = "PageFour";
    pageFiveName  = "PageFive";

    frontPagePath = PathParser.parse(frontPageName);
    pageOnePath   = PathParser.parse(pageOneName);
    pageTwoPath   = PathParser.parse(pageTwoName);
    pageThreePath = PathParser.parse(pageThreeName);
    pageFourPath  = PathParser.parse(pageFourName);
    pageFivePath  = PathParser.parse(pageFiveName);

	helloTag = "hello";
	worldTag = "world";
	fitnesseTag = "fitnesse";
  }

  @Test
  public void testTextPlain() throws Exception {
    Response r = responder.makeResponse(context, request);
    assertEquals("text/plain", r.getContentType());
  }

  @Test
  public void testPageNamesFromRoot() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "");
    WikiPageUtil.addPage(root, pageTwoPath, "");
    request.setResource("");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp(pageOneName, response.getContent());
    assertHasRegexp(pageTwoName, response.getContent());
  }

  @Test
  public void testPageNamesFromASubPage() throws Exception {
    WikiPage frontPage = WikiPageUtil.addPage(root, frontPagePath, "");
    WikiPageUtil.addPage(frontPage, pageOnePath, "");
    WikiPageUtil.addPage(frontPage, pageTwoPath, "");
    request.setResource("");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp(frontPageName, response.getContent());
    assertDoesntHaveRegexp(pageOneName, response.getContent());
    assertDoesntHaveRegexp(pageTwoName, response.getContent());

    request.setResource(frontPageName);
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp(pageOneName, response.getContent());
    assertHasRegexp(pageTwoName, response.getContent());
    assertDoesntHaveRegexp(frontPageName, response.getContent());
  }

  @Test
  public void jsonFormat() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath, "");
    WikiPageUtil.addPage(root, pageTwoPath, "");
    request.setResource("");
    request.addInput("format", "json");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    JSONArray actual = new JSONArray(response.getContent());
    assertEquals(2, actual.length());
    Set<String> actualSet = new HashSet<>();
    actualSet.add(actual.getString(0));
    actualSet.add(actual.getString(1));
    Set<String> expectedSet = new HashSet<>();
    expectedSet.add(pageOneName);
    expectedSet.add(pageTwoName);
    assertEquals(expectedSet, actualSet);
  }

  @Test
  public void canShowChildCount() throws Exception {
    WikiPage frontPage = WikiPageUtil.addPage(root, frontPagePath, "");
    WikiPageUtil.addPage(frontPage, pageOnePath, "");
    WikiPageUtil.addPage(frontPage, pageTwoPath, "");
    request.setResource("");
    request.addInput("ShowChildCount","");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("FrontPage 2", response.getContent());
  }

  private static int CountLines(String s) {
    if(s == null) { return 0; }
    return s.split("\r\n|\r|\n").length;
  }

  private void createTestPageTree() throws Exception {
	// FrontPage
	// + PageOne
	// | + PageTwo (hello)
	// + PageThree (world)
	//   + PageFour
	//     + PageFive (fitnesse)

    WikiPage frontPage = WikiPageUtil.addPage(root,      frontPagePath, "");
    WikiPage pageOne   = WikiPageUtil.addPage(frontPage, pageOnePath, "");
    WikiPage pageTwo   = WikiPageUtil.addPage(pageOne,   pageTwoPath, "");
    WikiPage pageThree = WikiPageUtil.addPage(frontPage, pageThreePath, "");
    WikiPage pageFour  = WikiPageUtil.addPage(pageThree, pageFourPath, "");
    WikiPage pageFive  = WikiPageUtil.addPage(pageFour,  pageFivePath, "");

    setTag(pageTwo, helloTag);
    setTag(pageThree, worldTag);
    setTag(pageFive, fitnesseTag);

    assertEquals(helloTag,    pageTwo.getData().getAttribute(PageData.PropertySUITES));
    assertEquals(worldTag,    pageThree.getData().getAttribute(PageData.PropertySUITES));
    assertEquals(fitnesseTag, pageFive.getData().getAttribute(PageData.PropertySUITES));
  }

  private void setTag(WikiPage page, String tag) {
    PageData data = page.getData();
    data.setAttribute(PageData.PropertySUITES, tag);
    page.commit(data);
  }

  @Test
  public void canBeUsedRecursively() throws Exception {
    createTestPageTree();

    request.setResource(frontPageName);
    request.addInput("Recursive", "");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    assertHasRegexp(pageOneName,                                             response.getContent());
    assertHasRegexp(pageOneName + "." + pageTwoName,                         response.getContent());
    assertHasRegexp(pageThreeName,                                           response.getContent());
    assertHasRegexp(pageThreeName + "." + pageFourName,                      response.getContent());
    assertHasRegexp(pageThreeName + "." + pageFourName + "." + pageFiveName, response.getContent());
    assertEquals(5, CountLines(response.getContent()));
  }

  @Test
  public void canReportOnlyLeaves() throws Exception {
    createTestPageTree();

    request.setResource(frontPageName);
    request.addInput("Recursive", "");
    request.addInput("LeafOnly", "");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(2, CountLines(response.getContent())); // we only have 2 leave pages
  }

  @Test
  public void canShowTags() throws Exception {
    createTestPageTree();

    request.setResource(frontPageName);
    request.addInput("Recursive", "");
    request.addInput("ShowTags", "");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    // since the setAttribute() calls in createTestPageTree() don't have an effect the following tests are failing
    // reenable them once the issue above has been resolved!
    assertHasRegexp(pageOneName, response.getContent());
    assertHasRegexp(pageOneName + "." + pageTwoName + " \\[" + helloTag + "]", response.getContent());
    assertHasRegexp(pageThreeName + " \\[" + worldTag + "\\]", response.getContent());
    assertHasRegexp(pageThreeName + "." + pageFourName + " \\[" + worldTag + "\\]", response.getContent());
    assertHasRegexp(pageThreeName + "." + pageFourName + "." + pageFiveName + " \\[" + fitnesseTag + "\\]\\[" + worldTag + "\\]", response.getContent());
  }
}
