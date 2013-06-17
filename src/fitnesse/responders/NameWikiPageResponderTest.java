// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wiki.mem.InMemoryPage;
import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;

import java.util.HashSet;
import java.util.Set;

public class NameWikiPageResponderTest {
  private WikiPage root;
  private NameWikiPageResponder responder;
  private MockRequest request;
  private String pageOneName;
  private String pageTwoName;
  private String frontPageName;
  private WikiPagePath pageOnePath;
  private WikiPagePath pageTwoPath;
  private WikiPagePath frontPagePath;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    responder = new NameWikiPageResponder();
    request = new MockRequest();

    pageOneName = "PageOne";
    pageTwoName = "PageTwo";
    frontPageName = "FrontPage";

    pageOnePath = PathParser.parse(pageOneName);
    pageTwoPath = PathParser.parse(pageTwoName);
    frontPagePath = PathParser.parse(frontPageName);
  }

  @Test
  public void testTextPlain() throws Exception {

    Response r = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals("text/plain", r.getContentType());
  }

  @Test
  public void testPageNamesFromRoot() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath);
    WikiPageUtil.addPage(root, pageTwoPath);
    request.setResource("");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertHasRegexp(pageOneName, response.getContent());
    assertHasRegexp(pageTwoName, response.getContent());
  }

  @Test
  public void testPageNamesFromASubPage() throws Exception {
    WikiPage frontPage = WikiPageUtil.addPage(root, frontPagePath);
    WikiPageUtil.addPage(frontPage, pageOnePath);
    WikiPageUtil.addPage(frontPage, pageTwoPath);
    request.setResource("");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertHasRegexp(frontPageName, response.getContent());
    assertDoesntHaveRegexp(pageOneName, response.getContent());
    assertDoesntHaveRegexp(pageTwoName, response.getContent());

    request.setResource(frontPageName);
    response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertHasRegexp(pageOneName, response.getContent());
    assertHasRegexp(pageTwoName, response.getContent());
    assertDoesntHaveRegexp(frontPageName, response.getContent());
  }

  @Test
  public void jsonFormat() throws Exception {
    WikiPageUtil.addPage(root, pageOnePath);
    WikiPageUtil.addPage(root, pageTwoPath);
    request.setResource("");
    request.addInput("format", "json");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    JSONArray actual = new JSONArray(response.getContent());
    assertEquals(2, actual.length());
    Set<String> actualSet = new HashSet<String>();
    actualSet.add(actual.getString(0));
    actualSet.add(actual.getString(1));
    Set<String> expectedSet = new HashSet<String>();
    expectedSet.add(pageOneName);
    expectedSet.add(pageTwoName);
    assertEquals(expectedSet, actualSet); 
  }

  @Test
  public void canShowChildCount() throws Exception {
    WikiPage frontPage = WikiPageUtil.addPage(root, frontPagePath);
    WikiPageUtil.addPage(frontPage, pageOnePath);
    WikiPageUtil.addPage(frontPage, pageTwoPath);
    request.setResource("");
    request.addInput("ShowChildCount","");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertHasRegexp("FrontPage 2", response.getContent());

  }
}
