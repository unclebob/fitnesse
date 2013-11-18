// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class ClassPathBuilderTest {
  private WikiPage root;
  private ClassPathBuilder builder;
  String pathSeparator = System.getProperty("path.separator");
  private WikiPagePath somePagePath;
  private static final String TEST_DIR = "testDir";

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    builder = new ClassPathBuilder();
    somePagePath = PathParser.parse("SomePage");
  }

  @Test
  public void testGetClasspath() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!path fitnesse.jar\n" +
                    "!path my.jar");
    String expected = "fitnesse.jar" + pathSeparator + "my.jar";
    assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
  }

  @Test
  public void testPathSeparatorVariable() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define PATH_SEPARATOR {|}\n" +
                    "!path fitnesse.jar\n" +
                    "!path my.jar");
    PageData data = page.getData();
    page.commit(data);

    String expected = "fitnesse.jar" + "|" + "my.jar";
    assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
  }

  @Test
  public void testGetPaths_OneLevel() throws Exception {
    String pageContent = "This is some content\n" +
      "!path aPath\n" +
      "end of conent\n";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), pageContent);
    String path = builder.getClasspath(page);
    assertEquals("aPath", path);
  }

  @Test
  public void testGetClassPathMultiLevel() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("ProjectOne"),
            "!path path2\n" +
                    "!path path 3");
    WikiPageUtil.addPage(root, PathParser.parse("ProjectOne.TesT"), "!path path1");
    PageCrawler pageCrawler = root.getPageCrawler();
    String cp = builder.getClasspath(pageCrawler.getPage(PathParser.parse("ProjectOne.TesT")));
    assertSubString("path1", cp);
    assertSubString("path2", cp);
    assertSubString("\"path 3\"", cp);
  }

  @Test
  public void testLinearClassPath() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage superPage = WikiPageUtil.addPage(root, PathParser.parse("SuperPage"), "!path superPagePath");
    WikiPage subPage = WikiPageUtil.addPage(superPage, PathParser.parse("SubPage"), "!path subPagePath");
    String cp = builder.getClasspath(subPage);
    assertEquals("subPagePath" + pathSeparator + "superPagePath", cp);
  }

  @Test
  public void testGetClassPathFromPageThatDoesntExist() throws Exception {
    String classPath = makeClassPathFromSimpleStructure("somePath");

    assertEquals("somePath", classPath);
  }

  private String makeClassPathFromSimpleStructure(String path) throws Exception {
    PageData data = root.getData();
    data.setContent("!path " + path);
    root.commit(data);
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.getPage(somePagePath, new MockingPageCrawler());
    String classPath = builder.getClasspath(page);
    return classPath;
  }

  @Test
  public void testThatPathsWithSpacesGetQuoted() throws Exception {
    WikiPageUtil.addPage(root, somePagePath, "!path Some File.jar");
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.getPage(somePagePath);

    assertEquals("\"Some File.jar\"", builder.getClasspath(page));

    WikiPageUtil.addPage(root, somePagePath, "!path somefile.jar\n!path Some Dir/someFile.jar");
    assertEquals("somefile.jar" + pathSeparator + "\"Some Dir/someFile.jar\"", builder.getClasspath(page));
  }

  @Test
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


  @Test
  public void testClasspath() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), "!path 123\n!path abc\n");
    String paths = builder.getClasspath(page);
    assertTrue(paths.contains("123"));
    assertTrue(paths.contains("abc"));
  }

  @Test
  public void testClasspathWithVariable() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");

    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), "!define PATH {/my/path}\n!path ${PATH}.jar");
    String paths = builder.getClasspath(page);
    assertEquals("/my/path.jar", paths.toString());

    PageData data = root.getData();
    data.setContent("!define PATH {/my/path}\n");
    root.commit(data);

    page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath2"), "!path ${PATH}.jar");
    paths = builder.getClasspath(page);
    assertEquals("/my/path.jar", paths.toString());
  }

  @Test
  public void testClasspathWithVariableDefinedInIncludedPage() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("VariablePage"), "!define PATH {/my/path}\n");

    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), "!include VariablePage\n!path ${PATH}.jar");
    String paths = builder.getClasspath(page);
    assertEquals("/my/path.jar", paths.toString());
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
