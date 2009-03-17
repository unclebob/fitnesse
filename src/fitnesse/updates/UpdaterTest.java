// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.File;

import fitnesse.wiki.PathParser;

public class UpdaterTest extends UpdateTestCase {

  public void setUp() throws Exception {
    super.setUp();
    UpdaterImplementation.testing = true;
    crawler.addPage(root, PathParser.parse("PageOne"));
  }

  public void testProperties() throws Exception {
    File file = new File("testDir/RooT/properties");
    assertFalse(file.exists());
    UpdaterImplementation updater = new UpdaterImplementation(context);
    updater.updates = new Update[]{};
    updater.update();
    assertTrue(file.exists());
  }
}
