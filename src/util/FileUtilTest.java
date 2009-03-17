// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.File;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {
  public void testCreateDir() throws Exception {
    File dir = FileUtil.createDir("temp");
    assertTrue(dir.exists());
    assertTrue(dir.isDirectory());
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testGetDirectoryListingEmpty() throws Exception {
    File dir = FileUtil.createDir("temp");
    assertEquals(0, FileUtil.getDirectoryListing(dir).length);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testOrganizeFilesOneFile() throws Exception {
    File dir = FileUtil.createDir("temp");
    File file = createFileInDir(dir, "file.txt");
    assertEquals(1, FileUtil.getDirectoryListing(dir).length);
    assertEquals(file, FileUtil.getDirectoryListing(dir)[0]);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testOrganizeFilesFiveFiles() throws Exception {
    File dir = FileUtil.createDir("temp");
    File file3 = createFileInDir(dir, "dFile.txt");
    File file1 = createFileInDir(dir, "bFile.txt");
    File file4 = createFileInDir(dir, "eFile.txt");
    File file0 = createFileInDir(dir, "aFile.txt");
    File file2 = createFileInDir(dir, "cFile.txt");
    assertEquals(5, FileUtil.getDirectoryListing(dir).length);
    assertEquals(file0, FileUtil.getDirectoryListing(dir)[0]);
    assertEquals(file1, FileUtil.getDirectoryListing(dir)[1]);
    assertEquals(file2, FileUtil.getDirectoryListing(dir)[2]);
    assertEquals(file3, FileUtil.getDirectoryListing(dir)[3]);
    assertEquals(file4, FileUtil.getDirectoryListing(dir)[4]);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testOrganizeFilesOneSubDir() throws Exception {
    File dir = FileUtil.createDir("temp");
    File subDir = createSubDir(dir, "subDir");
    assertEquals(1, FileUtil.getDirectoryListing(dir).length);
    assertEquals(subDir, FileUtil.getDirectoryListing(dir)[0]);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testOrganizeFilesFiveSubDirs() throws Exception {
    File dir = FileUtil.createDir("temp");
    File dir3 = createSubDir(dir, "dDir");
    File dir1 = createSubDir(dir, "bDir");
    File dir4 = createSubDir(dir, "eDir");
    File dir0 = createSubDir(dir, "aDir");
    File dir2 = createSubDir(dir, "cDir");
    assertEquals(5, FileUtil.getDirectoryListing(dir).length);
    assertEquals(dir0, FileUtil.getDirectoryListing(dir)[0]);
    assertEquals(dir1, FileUtil.getDirectoryListing(dir)[1]);
    assertEquals(dir2, FileUtil.getDirectoryListing(dir)[2]);
    assertEquals(dir3, FileUtil.getDirectoryListing(dir)[3]);
    assertEquals(dir4, FileUtil.getDirectoryListing(dir)[4]);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  public void testOrganizeFilesMixOfFilesAndDirs() {
    File dir = FileUtil.createDir("temp");
    File dir3 = createSubDir(dir, "dDir");
    File file3 = createFileInDir(dir, "dFile.txt");
    File file0 = createFileInDir(dir, "aFile.txt");
    File dir1 = createSubDir(dir, "bDir");
    File file4 = createFileInDir(dir, "eFile.txt");
    File dir4 = createSubDir(dir, "eDir");
    File dir0 = createSubDir(dir, "aDir");
    File file1 = createFileInDir(dir, "bFile.txt");
    File dir2 = createSubDir(dir, "cDir");
    File file2 = createFileInDir(dir, "cFile.txt");
    assertEquals(10, FileUtil.getDirectoryListing(dir).length);
    assertEquals(dir0, FileUtil.getDirectoryListing(dir)[0]);
    assertEquals(dir1, FileUtil.getDirectoryListing(dir)[1]);
    assertEquals(dir2, FileUtil.getDirectoryListing(dir)[2]);
    assertEquals(dir3, FileUtil.getDirectoryListing(dir)[3]);
    assertEquals(dir4, FileUtil.getDirectoryListing(dir)[4]);
    assertEquals(file0, FileUtil.getDirectoryListing(dir)[5]);
    assertEquals(file1, FileUtil.getDirectoryListing(dir)[6]);
    assertEquals(file2, FileUtil.getDirectoryListing(dir)[7]);
    assertEquals(file3, FileUtil.getDirectoryListing(dir)[8]);
    assertEquals(file4, FileUtil.getDirectoryListing(dir)[9]);
    FileUtil.deleteFileSystemDirectory(dir);
  }

  private File createFileInDir(File dir, String fileName) {
    return FileUtil.createFile(FileUtil.buildPath(new String[]{dir.getPath(), fileName}), "");
  }

  private File createSubDir(File dir, String subDirName) {
    return FileUtil.createDir(FileUtil.buildPath(new String[]{dir.getPath(), subDirName}));
  }

  public void testBuildPathEmpty() throws Exception {
    assertEquals("", FileUtil.buildPath(new String[]{}));
  }

  public void testBuildPathOneElement() throws Exception {
    assertEquals("a", FileUtil.buildPath(new String[]{"a"}));
  }

  public void testBuildPathThreeElements() throws Exception {
    String separator = System.getProperty("file.separator");
    assertEquals("a" + separator + "b" + separator + "c", FileUtil.buildPath(new String[]{"a", "b", "c"}));
  }

}

