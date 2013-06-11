// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wiki.mem.InMemoryPage;
import junit.framework.TestCase;

import static fitnesse.responders.versions.VersionResponderTest.last;

public class RollbackResponderTest extends TestCase {
  private WikiPage page;
  private Response response;

  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageBuilder pageBuilder = new PageBuilder();
    page = pageBuilder.addPage(root, PathParser.parse("PageOne"), "original content");
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

  public void tearDown() throws Exception {
  }

  public void testStuff() throws Exception {
    assertEquals(303, response.getStatus());
    assertEquals("PageOne", response.getHeader("Location"));

    PageData data = page.getData();
    assertEquals("original content", data.getContent());
    assertEquals(true, data.hasAttribute("Edit"));
  }
}
