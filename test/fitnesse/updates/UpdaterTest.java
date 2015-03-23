// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.File;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpdaterTest extends UpdateTestCase {

  public void setUp() throws Exception {
    super.setUp();
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"));
  }

  @Test
  public void testProperties() throws Exception {
    File file = new File(testDir, "properties");
    assertFalse(file.exists());
    updater.setUpdates(new Update[]{});
    updater.update();
    assertTrue(file.exists());
  }
}
