// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wiki.mem.InMemoryPage;
import util.RegexTestCase;

public class VersionSelectionResponderTest extends RegexTestCase {
  private WikiPage page;
  private WikiPage root;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    PageBuilder pageBuilder = new PageBuilder();
    page = pageBuilder.addPage(root, PathParser.parse("PageOne"), "some content");
    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES,"Page One tags");
    page.commit(data);
    FitNesseUtil.makeTestContext(root);
  }

  public void tearDown() throws Exception {
  }

  public void ignore_testGetVersionsList() throws Exception {
    // TODO: create page with test versions controller and let it return versions in arbitraty order
    Set<VersionInfo> set = new HashSet<VersionInfo>();
    VersionInfo v1 = new VersionInfo("1-12345678901234", "", new Date(12345678901234L * 1000));
    VersionInfo v2 = new VersionInfo("2-45612345678901", "", new Date(45612345678901L * 1000));
    VersionInfo v3 = new VersionInfo("3-11112345678901", "", new Date(11112345678901L * 1000));
    VersionInfo v4 = new VersionInfo("4-12212345465679", "", new Date(12212345465679L * 1000));
    set.add(v1);
    set.add(v2);
    set.add(v3);
    set.add(v4);

    List<VersionInfo> list = VersionSelectionResponder.getVersionsList(page);
    assertEquals(v3, list.get(3));
    assertEquals(v4, list.get(2));
    assertEquals(v1, list.get(1));
    assertEquals(v2, list.get(0));
  }

  public void testMakeReponder() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new VersionSelectionResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    String content = response.getContent();
    assertSubString("<a", content);
    assertSubString("?responder=viewVersion", content);
    assertNotSubString("$version", content);
    assertSubString("<h5> Page One tags</h5>", content);
  }
}
