// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fitnesse.testrunner.ClassPathBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    ClassPathBuilder.Wildcard wildcard = new ClassPathBuilder.Wildcard("*.jar");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(2, files.length);
    assertTrue(list.contains("one.jar"));
    assertTrue(list.contains("two.jar"));
  }

  @Test
  public void testDll() throws Exception {
    ClassPathBuilder.Wildcard wildcard = new ClassPathBuilder.Wildcard("*.dll");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(2, files.length);
    assertTrue(list.contains("one.dll"));
    assertTrue(list.contains("two.dll"));
  }

  @Test
  public void testOne() throws Exception {
    ClassPathBuilder.Wildcard wildcard = new ClassPathBuilder.Wildcard("one*");
    File[] files = testDir.listFiles(wildcard);
    List<String> list = fileArrayToStringList(files);
    assertEquals(3, files.length);
    assertTrue(list.contains("oneA"));
    assertTrue(list.contains("one.jar"));
    assertTrue(list.contains("one.dll"));
  }

  @Test
  public void testAll() throws Exception {
    ClassPathBuilder.Wildcard wildcard = new ClassPathBuilder.Wildcard("*");
    File[] files = testDir.listFiles(wildcard);
    assertEquals(6, files.length);
  }

  private List<String> fileArrayToStringList(File[] files) {
    List<String> list = new ArrayList<>();
    for (File file : files) {
      list.add(file.getName());
    }
    return list;
  }

  public static void makeSampleFiles() throws IOException {
    FileUtil.makeDir(TEST_DIR);
    FileUtil.createFile(TEST_DIR + "/one.jar", "");
    FileUtil.createFile(TEST_DIR + "/two.jar", "");
    FileUtil.createFile(TEST_DIR + "/one.dll", "");
    FileUtil.createFile(TEST_DIR + "/two.dll", "");
    FileUtil.createFile(TEST_DIR + "/oneA", "");
    FileUtil.createFile(TEST_DIR + "/twoA", "");
  }

  public static void deleteSampleFiles() throws IOException {
    FileUtil.deleteFileSystemDirectory(TEST_DIR);
  }


}
