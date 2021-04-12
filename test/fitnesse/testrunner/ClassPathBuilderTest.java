// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

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
    List<String> classPath = builder.getClassPath(root.getChildPage("TestPage"));
    assertEquals("fitnesse.jar", classPath.get(0));
    assertEquals("my.jar", classPath.get(1));
  }

  @Test
  public void testGetPaths_OneLevel() throws Exception {
    String pageContent = "This is some content\n" +
      "!path aPath\n" +
      "end of conent\n";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), pageContent);
    List<String> path = builder.getClassPath(page);
    assertEquals("aPath", path.get(0));
  }

  @Test
  public void testGetClassPathMultiLevel() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("ProjectOne"),
            "!path path2\n" +
                    "!path path 3");
    WikiPageUtil.addPage(root, PathParser.parse("ProjectOne.TesT"), "!path path1");
    PageCrawler pageCrawler = root.getPageCrawler();
    List<String> cp = builder.getClassPath(pageCrawler.getPage(PathParser.parse("ProjectOne.TesT")));
    assertSubString("path1", cp.get(0));
    assertSubString("path2", cp.get(1));
    assertSubString("path 3", cp.get(2));
  }

  @Test
  public void testLinearClassPath() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage superPage = WikiPageUtil.addPage(root, PathParser.parse("SuperPage"), "!path superPagePath");
    WikiPage subPage = WikiPageUtil.addPage(superPage, PathParser.parse("SubPage"), "!path subPagePath");
    List<String> cp = builder.getClassPath(subPage);
    assertEquals("subPagePath", cp.get(0));
    assertEquals("superPagePath", cp.get(1));
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
    List<String> classPath = builder.getClassPath(page);
    return StringUtils.join(classPath, System.getProperty("path.separator"));
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
    List<String> paths = builder.getClassPath(page);
    assertTrue(paths.contains("123"));
    assertTrue(paths.contains("abc"));
  }

  @Test
  public void testClasspathWithVariable() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");

    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), "!define PATH {/my/path}\n!path ${PATH}.jar");
    List<String> paths = builder.getClassPath(page);
    assertEquals("/my/path.jar", paths.get(0));

    PageData data = root.getData();
    data.setContent("!define PATH {/my/path}\n");
    root.commit(data);

    page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath2"), "!path ${PATH}.jar");
    paths = builder.getClassPath(page);
    assertEquals("/my/path.jar", paths.get(0));
  }

  @Test
  public void testClasspathWithVariableDefinedInIncludedPage() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("VariablePage"), "!define PATH {/my/path}\n");

    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ClassPath"), "!include VariablePage\n!path ${PATH}.jar");
    List<String> paths = builder.getClassPath(page);
    assertEquals("/my/path.jar", paths.get(0));
  }

  public static void makeSampleFiles() throws IOException {
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

  public static void deleteSampleFiles() throws IOException {
    FileUtil.deleteFileSystemDirectory(TEST_DIR);
  }
}
