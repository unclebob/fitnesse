// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class VersionResponderTest extends RegexTestCase {
  private String oldVersion;
  private SimpleResponse response;
  private WikiPage root;
  private WikiPage page;

  private void makeTestResponse(String pageName) throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    page = root.getPageCrawler().addPage(root, PathParser.parse(pageName), "original content");
    PageData data = page.getData();
    
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "New Page tags");
    data.setContent("new stuff");
    VersionInfo commitRecord = page.commit(data);
    oldVersion = commitRecord.getName();

    MockRequest request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", oldVersion);

    Responder responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
  }

  public void testVersionName() throws Exception {
    makeTestResponse("PageOne");

    assertHasRegexp("original content", response.getContent());
    assertDoesntHaveRegexp("new stuff", response.getContent());
    assertHasRegexp(oldVersion, response.getContent());
    assertNotSubString("New Page tags", response.getContent());
  }

  public void testButtons() throws Exception {
    makeTestResponse("PageOne");

    assertDoesntHaveRegexp("Edit button", response.getContent());
    assertDoesntHaveRegexp("Search button", response.getContent());
    assertDoesntHaveRegexp("Test button", response.getContent());
    assertDoesntHaveRegexp("Suite button", response.getContent());
    assertDoesntHaveRegexp("Versions button", response.getContent());

    assertHasRegexp(">Rollback</a>", response.getContent());
  }

  public void testNameNoAtRootLevel() throws Exception {
    makeTestResponse("PageOne.PageTwo");
    assertSubString("PageOne.PageTwo?responder=", response.getContent());
  }
}
