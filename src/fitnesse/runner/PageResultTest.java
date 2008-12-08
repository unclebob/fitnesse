// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fitnesse.responders.run.TestSummary;
import junit.framework.TestCase;

public class PageResultTest extends TestCase {
  public void testToString() throws Exception {
    PageResult result = new PageResult("PageTitle", new TestSummary(1, 2, 3, 4), "content");
    assertEquals("PageTitle\n1 right, 2 wrong, 3 ignored, 4 exceptions\ncontent", result.toString());
  }

  public void testParse() throws Exception {
    TestSummary testSummary = new TestSummary(1, 2, 3, 4);
    PageResult result = new PageResult("PageTitle", testSummary, "content");
    PageResult parsedResult = PageResult.parse(result.toString());
    assertEquals("PageTitle", parsedResult.title());
    assertEquals(testSummary, parsedResult.testSummary());
    assertEquals("content", parsedResult.content());
  }
}
