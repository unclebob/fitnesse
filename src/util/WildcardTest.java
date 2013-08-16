// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WildcardTest {
  private File testDir;
  private static final String TEST_DIR = "testDir";

  @Before
  public void setUp() throws Exception {
    deleteSampleFiles();
    makeSampleFiles();
    testDir = new File(TEST_DIR);
  }

  @After
  public void tearDown() throws Exception {
    deleteSampleFiles();
  }

  @Test
  public void testJar() throws Exception {
    Wildcard wildcard = new Wildcard("*.jar");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(2, files.length);
    assertTrue(list.contains("one.jar"));
    assertTrue(list.contains("two.jar"));
  }

  @Test
  public void testDll() throws Exception {
    Wildcard wildcard = new Wildcard("*.dll");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(2, files.length);
    assertTrue(list.contains("one.dll"));
    assertTrue(list.contains("two.dll"));
  }

  @Test
  public void testOne() throws Exception {
    Wildcard wildcard = new Wildcard("one*");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(3, files.length);
    assertTrue(list.contains("oneA"));
    assertTrue(list.contains("one.jar"));
    assertTrue(list.contains("one.dll"));
  }

  @Test
  public void testAll() throws Exception {
    Wildcard wildcard = new Wildcard("*");
    File[] files = testDir.listFiles(wildcard);
    assertEquals(6, files.length);
  }

  private List<String> fileArrayToStringList(File[] files) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      list.add(file.getName());
    }
    return list;
  }

  public static void makeSampleFiles() {
    FileUtil.makeDir(TEST_DIR);
    FileUtil.createFile(TEST_DIR + "/one.jar", "");
    FileUtil.createFile(TEST_DIR + "/two.jar", "");
    FileUtil.createFile(TEST_DIR + "/one.dll", "");
    FileUtil.createFile(TEST_DIR + "/two.dll", "");
    FileUtil.createFile(TEST_DIR + "/oneA", "");
    FileUtil.createFile(TEST_DIR + "/twoA", "");
  }

  public static void deleteSampleFiles() {
    FileUtil.deleteFileSystemDirectory(TEST_DIR);
  }


}
