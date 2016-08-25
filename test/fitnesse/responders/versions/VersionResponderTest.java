// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.util.Collection;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import org.junit.Test;

public class VersionResponderTest {
  private String oldVersion;
  private SimpleResponse response;

  private void makeTestResponse(String pageName) throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse(pageName), "original content ${requestParam}");
    PageData data = page.getData();

    WikiPageProperty properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "New Page tags");
    data.setContent("new stuff");
    VersionInfo commitRecord = last(page.getVersions());
    oldVersion = commitRecord.getName();
    page.commit(data);
    MockRequest request = new MockRequest();
    request.setResource(pageName);
    request.addInput("version", oldVersion);
    request.addInput("requestParam", "requestValue");

    Responder responder = new VersionResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
  }

  @Test
  public void testVersionName() throws Exception {
    makeTestResponse("PageOne");

    assertHasRegexp("original content requestValue", response.getContent());
    assertDoesntHaveRegexp("new stuff", response.getContent());
    assertHasRegexp(oldVersion, response.getContent());
    assertNotSubString("New Page tags", response.getContent());
  }

  @Test
  public void testButtons() throws Exception {
    makeTestResponse("PageOne");

    assertDoesntHaveRegexp("Edit button", response.getContent());
    assertDoesntHaveRegexp("Search button", response.getContent());
    assertDoesntHaveRegexp("Test button", response.getContent());
    assertDoesntHaveRegexp("Suite button", response.getContent());
    assertDoesntHaveRegexp("Versions button", response.getContent());

    assertHasRegexp(">Rollback</a>", response.getContent());
  }

  @Test
  public void testNameNoAtRootLevel() throws Exception {
    makeTestResponse("PageOne.PageTwo");
    assertSubString("PageOne.PageTwo?responder=", response.getContent());
  }

  static VersionInfo last(Collection<VersionInfo> versions) {
    VersionInfo last = null;
    for (VersionInfo i : versions) {
      last = i;
    }
    return last;
  }


}
