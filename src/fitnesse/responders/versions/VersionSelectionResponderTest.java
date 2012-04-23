// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class VersionSelectionResponderTest extends RegexTestCase {
  private WikiPage page;
  private WikiPage root;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "some content");
    FitNesseUtil.makeTestContext(root);
  }

  public void tearDown() throws Exception {
  }

  public void testGetVersionsList() throws Exception {
    Set<VersionInfo> set = new HashSet<VersionInfo>();
    VersionInfo v1 = new VersionInfo("1-12345678901234");
    VersionInfo v2 = new VersionInfo("2-45612345678901");
    VersionInfo v3 = new VersionInfo("3-11112345678901");
    VersionInfo v4 = new VersionInfo("4-12212345465679");
    set.add(v1);
    set.add(v2);
    set.add(v3);
    set.add(v4);

    PageData data = new PageData(page);
    data.addVersions(set);

    List<VersionInfo> list = VersionSelectionResponder.getVersionsList(data);
    assertEquals(v3, list.get(3));
    assertEquals(v4, list.get(2));
    assertEquals(v1, list.get(1));
    assertEquals(v2, list.get(0));
  }

  public void testMakeReponder() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new VersionSelectionResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

    String content = response.getContent();
    assertSubString("<a", content);
    assertSubString("?responder=viewVersion", content);
    assertNotSubString("$version", content);
  }
}
