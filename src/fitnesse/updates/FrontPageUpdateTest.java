// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class FrontPageUpdateTest extends UpdateTestCase {
  protected Update makeUpdate() throws Exception {
    return new FrontPageUpdate(updater);
  }

  public void testShouldUpdate() throws Exception {
    assertTrue(update.shouldBeApplied());
    updater.getRoot().addChildPage("FrontPage");
    assertFalse(update.shouldBeApplied());
  }

  public void testProperties() throws Exception {
    update.doUpdate();
    WikiPage page = updater.getRoot().getChildPage("FrontPage");
    assertNotNull(page);

    PageData data = page.getData();
    assertTrue(data.hasAttribute("Edit"));
    assertTrue(data.hasAttribute("Properties"));
  }
}
