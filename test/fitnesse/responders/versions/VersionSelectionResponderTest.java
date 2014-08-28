// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class VersionSelectionResponderTest {
  private WikiPage page;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "some content");
    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES,"Page One tags");
    page.commit(data);
    FitNesseUtil.makeTestContext(root);
  }

  @Test
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
