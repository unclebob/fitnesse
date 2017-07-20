// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

public class VersionSelectionResponderTest {
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("PageOne"), "some content");
    PageData data = page.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Page One tags");
    page.commit(data);
  }

  @Test
  public void testMakeReponder() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new VersionSelectionResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    String content = response.getContent();
    assertSubString("<a", content);
    assertSubString("?responder=viewVersion", content);
    assertNotSubString("$version", content);
    assertSubString("<span class=\"tag\">Page One tags</span>", content);
  }
}
