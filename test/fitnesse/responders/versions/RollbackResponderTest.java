// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import static fitnesse.responders.versions.VersionResponderTest.last;
import static org.junit.Assert.assertEquals;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.WikiPageProperties;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Test;

public class RollbackResponderTest {

  @Test
  public void testStuff() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("PageOne"), "original content");
    PageData data = page.getData();
    data.setContent("new stuff");
    data.setProperties(new WikiPageProperties());
    VersionInfo commitRecord = last(page.getVersions());
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("version", commitRecord.getName());

    Responder responder = new RollbackResponder();
    Response response = responder.makeResponse(context, request);

    assertEquals(303, response.getStatus());
    assertEquals("/PageOne", response.getHeader("Location"));

    PageData data2 = page.getData();
    assertEquals("original content", data2.getContent());
    assertEquals(true, data2.hasAttribute("Edit"));
  }
}
