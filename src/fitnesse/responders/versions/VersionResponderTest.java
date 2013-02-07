// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.util.ArrayList;

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
  
  public void testPageWithMultipleVersionsForNavigationButtons() throws Exception {
    String pageName = "MultiVersionPage";
    
    root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    page = root.getPageCrawler().addPage(root, PathParser.parse(pageName), "original base content");
    PageData data = page.getData();
    
    VersionInfo base = new ArrayList<VersionInfo>(page.getData().getVersions()).get(0);
    
    data.setContent("updated content");
    VersionInfo update1 = page.commit(data);

    data.setContent("futher update to content");
    VersionInfo update2 = page.commit(data);

    data.setContent("latest content");
    VersionInfo update3 = page.commit(data);

    // check base version has next but no previous button
    MockRequest request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", base.getName());

    Responder responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    
    assertHasRegexp(">Next</a>", response.getContent());
    assertDoesntHaveRegexp(">Previous</a>", response.getContent());

    // check 'updated content' version has both next and previous button
    request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", update1.getName());

    responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    
    assertHasRegexp(">Next</a>", response.getContent());
    assertHasRegexp(">Previous</a>", response.getContent());

    // check 'further updated to content' version has both next and previous button
    request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", update2.getName());

    responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    
    assertHasRegexp(">Next</a>", response.getContent());
    assertHasRegexp(">Previous</a>", response.getContent());

    // check 'latest content' version has both next and previous button
    request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", update3.getName());

    responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    
    assertDoesntHaveRegexp(">Next</a>", response.getContent());
    assertHasRegexp(">Previous</a>", response.getContent());
  }

}
