// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import static fitnesse.responders.versions.VersionResponderTest.last;
import static org.junit.Assert.assertEquals;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class RollbackResponderTest {
  private WikiPage page;
  private Response response;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "original content");
    PageData data = page.getData();
    data.setContent("new stuff");
    data.setProperties(new WikiPageProperties());
    VersionInfo commitRecord = last(page.getVersions());
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("version", commitRecord.getName());

    Responder responder = new RollbackResponder();
    response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
  }

  @Test
  public void testStuff() throws Exception {
    assertEquals(303, response.getStatus());
    assertEquals("PageOne", response.getHeader("Location"));

    PageData data = page.getData();
    assertEquals("original content", data.getContent());
    assertEquals(true, data.hasAttribute("Edit"));
  }
}
