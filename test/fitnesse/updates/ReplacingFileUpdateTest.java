// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;

public class ReplacingFileUpdateTest extends UpdateTestCase {
  public final File sourceFile = new File("build/classes/test", "testFile");

  public final String destDirName = "subDir";
  public File destFile;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    destFile = new File(new File(testDir.getPath(), destDirName), "testFile");
    sourceFile.createNewFile();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    sourceFile.delete();
  }

  @Override
  protected Update makeUpdate() throws Exception {
    return new ReplacingFileUpdate("testFile", new File(context.getRootPagePath(), destDirName));
  }

  @Test
  public void testNoDestination() throws Exception {
    assertTrue(update.shouldBeApplied());
    update.doUpdate();
    assertTrue(destFile.exists());
  }

  @Test
  public void testFileMatch() throws Exception {
    update.doUpdate();
    assertFalse(update.shouldBeApplied());
  }

  @Test
  public void testFileDiffer() throws Exception {
    update.doUpdate();

    FileOutputStream output = new FileOutputStream(sourceFile);
    output.write("hello".getBytes());
    output.close();

    assertTrue(update.shouldBeApplied());
    update.doUpdate();

    assertEquals("hello", FileUtil.getFileContent(destFile));
  }
}
