// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.File;
import java.io.IOException;

import util.FileUtil;

public class SampleFileUtility {
  public static File filesDir;
  public static File testDir;
  public static File testFile1;
  public static File testFile2;
  public static File testFile3;
  public static File testFile4;

  public static void makeSampleFiles(String rootPagePath) throws IOException {
    File dir = new File(FitNesseUtil.base);
    dir.mkdir();
    filesDir = new File(dir, "files");
    filesDir.mkdir();
    testDir = new File(filesDir, "testDir");
    testDir.mkdir();

    testFile1 = FileUtil.createFile(rootPagePath + "/files/testFile1", "file1 content");
    testFile2 = FileUtil.createFile(rootPagePath + "/files/testDir/testFile2", "file2 content");
    testFile3 = FileUtil.createFile(rootPagePath + "/files/testDir/testFile3", "file3 content");
    testFile4 = FileUtil.createFile(rootPagePath + "/files/file4 with spaces.txt", "file4 content");
  }

  public static void deleteSampleFiles(String rootPagePath) throws IOException {
    FileUtil.deleteFileSystemDirectory(rootPagePath);
  }

  public static void addFile(String rootPagePath, String name, String content) throws IOException {
    FileUtil.createFile(rootPagePath + name, content);
  }
}
