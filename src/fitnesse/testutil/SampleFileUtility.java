// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.File;

import util.FileUtil;

public class SampleFileUtility {
  public static final String base = FitNesseUtil.base;
  public static File filesDir;
  public static File testDir;
  public static File testFile1;
  public static File testFile2;
  public static File testFile3;
  public static File testFile4;

  public static void makeSampleFiles() {
    File dir = new File(FitNesseUtil.base);
    dir.mkdir();
    filesDir = new File(dir, "files");
    filesDir.mkdir();
    testDir = new File(filesDir, "testDir");
    testDir.mkdir();

    testFile1 = FileUtil.createFile(FitNesseUtil.base + "/files/testFile1", "file1 content");
    testFile2 = FileUtil.createFile(FitNesseUtil.base + "/files/testDir/testFile2", "file2 content");
    testFile3 = FileUtil.createFile(FitNesseUtil.base + "/files/testDir/testFile3", "file3 content");
    testFile4 = FileUtil.createFile(FitNesseUtil.base + "/files/file4 with spaces.txt", "file4 content");
  }

  public static void deleteSampleFiles() {
    FileUtil.deleteFileSystemDirectory(FitNesseUtil.base);
  }

  public static void addFile(String name, String content) {
    FileUtil.createFile(FitNesseUtil.base + name, content);
  }
}
