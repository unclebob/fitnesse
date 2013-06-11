// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.*;
import util.FileUtil;
import util.RegexTestCase;
import fitnesse.wiki.mem.InMemoryPage;

public class ClassPathBuilderTest extends RegexTestCase {
  private WikiPage root;
  private ClassPathBuilder builder;
  String pathSeparator = System.getProperty("path.separator");
  private PageBuilder pageBuilder;
  private WikiPagePath somePagePath;
  private static final String TEST_DIR = "testDir";

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageBuilder = new PageBuilder();
    builder = new ClassPathBuilder();
    somePagePath = PathParser.parse("SomePage");
  }

  public void testGetClasspath() throws Exception {
    pageBuilder.addPage(root, PathParser.parse("TestPage"),
            "!path fitnesse.jar\n" +
                    "!path my.jar");
    String expected = "fitnesse.jar" + pathSeparator + "my.jar";
    assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
  }

  public void testPathSeparatorVariable() throws Exception {
    WikiPage page = pageBuilder.addPage(root, PathParser.parse("TestPage"),
      "!define PATH_SEPARATOR {|}\n" +
      "!path fitnesse.jar\n" +
        "!path my.jar");
    PageData data = page.getData();
    page.commit(data);

    String expected = "fitnesse.jar" + "|" + "my.jar";
    assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
  }

  public void testGetPaths_OneLevel() throws Exception {
    String pageContent = "This is some content\n" +
      "!path aPath\n" +
      "end of conent\n";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = pageBuilder.addPage(root, PathParser.parse("ClassPath"), pageContent);
    String path = builder.getClasspath(page);
    assertEquals("aPath", path);
  }

  public void testGetClassPathMultiLevel() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    pageBuilder.addPage(root, PathParser.parse("ProjectOne"),
            "!path path2\n" +
                    "!path path 3");
    pageBuilder.addPage(root, PathParser.parse("ProjectOne.TesT"), "!path path1");
    PageCrawler pageCrawler = root.getPageCrawler();
    String cp = builder.getClasspath(pageCrawler.getPage(root, PathParser.parse("ProjectOne.TesT")));
    assertSubString("path1", cp);
    assertSubString("path2", cp);
    assertSubString("\"path 3\"", cp);
  }

  public void testLinearClassPath() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage superPage = pageBuilder.addPage(root, PathParser.parse("SuperPage"), "!path superPagePath");
    WikiPage subPage = pageBuilder.addPage(superPage, PathParser.parse("SubPage"), "!path subPagePath");
    String cp = builder.getClasspath(subPage);
    assertEquals("subPagePath" + pathSeparator + "superPagePath", cp);

  }

  public void testGetClassPathFromPageThatDoesntExist() throws Exception {
    String classPath = makeClassPathFromSimpleStructure("somePath");

    assertEquals("somePath", classPath);
  }

  private String makeClassPathFromSimpleStructure(String path) throws Exception {
    PageData data = root.getData();
    data.setContent("!path " + path);
    root.commit(data);
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.getPage(root, somePagePath, new MockingPageCrawler());
    String classPath = builder.getClasspath(page);
    return classPath;
  }

  public void testThatPathsWithSpacesGetQuoted() throws Exception {
    pageBuilder.addPage(root, somePagePath, "!path Some File.jar");
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.getPage(root, somePagePath);

    assertEquals("\"Some File.jar\"", builder.getClasspath(page));

    pageBuilder.addPage(root, somePagePath, "!path somefile.jar\n!path Some Dir/someFile.jar");
    assertEquals("somefile.jar" + pathSeparator + "\"Some Dir/someFile.jar\"", builder.getClasspath(page));
  }

  public void testWildCardExpansion() throws Exception {
    try {
      makeSampleFiles();

      String classPath = makeClassPathFromSimpleStructure("testDir/*.jar");
      assertHasRegexp("one\\.jar", classPath);
      assertHasRegexp("two\\.jar", classPath);

      classPath = makeClassPathFromSimpleStructure("testDir/*.dll");
      assertHasRegexp("one\\.dll", classPath);
      assertHasRegexp("two\\.dll", classPath);

      classPath = makeClassPathFromSimpleStructure("testDir/one*");
      assertHasRegexp("one\\.dll", classPath);
      assertHasRegexp("one\\.jar", classPath);
      assertHasRegexp("oneA", classPath);

      classPath = makeClassPathFromSimpleStructure("testDir/**.jar");
      assertHasRegexp("one\\.jar", classPath);
      assertHasRegexp("two\\.jar", classPath);
      assertHasRegexp("subdir(?:\\\\|/)sub1\\.jar", classPath);
      assertHasRegexp("subdir(?:\\\\|/)sub2\\.jar", classPath);
    }
    finally {
      deleteSampleFiles();
    }
  }

  public static void makeSampleFiles() {
    FileUtil.makeDir(TEST_DIR);
    FileUtil.createFile(TEST_DIR + "/one.jar", "");
    FileUtil.createFile(TEST_DIR + "/two.jar", "");
    FileUtil.createFile(TEST_DIR + "/one.dll", "");
    FileUtil.createFile(TEST_DIR + "/two.dll", "");
    FileUtil.createFile(TEST_DIR + "/oneA", "");
    FileUtil.createFile(TEST_DIR + "/twoA", "");
    FileUtil.createDir(TEST_DIR + "/subdir");
    FileUtil.createFile(TEST_DIR + "/subdir/sub1.jar", "");
    FileUtil.createFile(TEST_DIR + "/subdir/sub2.jar", "");
    FileUtil.createFile(TEST_DIR + "/subdir/sub1.dll", "");
    FileUtil.createFile(TEST_DIR + "/subdir/sub2.dll", "");
  }

  public static void deleteSampleFiles() {
    FileUtil.deleteFileSystemDirectory(TEST_DIR);
  }
}
