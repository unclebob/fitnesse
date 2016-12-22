// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki.fs;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import fitnesse.util.Clock;
import fitnesse.wiki.*;
import util.FileUtil;

import static fitnesse.wiki.WikiPageProperty.EDIT;
import static fitnesse.wiki.WikiPageProperty.FILES;
import static fitnesse.wiki.WikiPageProperty.SEARCH;
import static fitnesse.wiki.WikiPageProperty.VERSIONS;
import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileSystemPageTest {
  private FileSystem fileSystem;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    fileSystem = new MemoryFileSystem();
    fileSystem.makeFile(new File("RooT", "content.txt"), "");
    Properties properties = new Properties();
    properties.setProperty("wiki.page.old.style", "true");
    root = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem),
      FileSystemPageFactory.InnerFileSystemPageFactory.class).makePage(new File("RooT"), "RooT", null, new SystemVariableSource(properties));
  }

  @Test
  public void testCreateBase() throws Exception {
    FileBasedWikiPage levelA = (FileBasedWikiPage) WikiPageUtil.addPage(root, PathParser.parse("PageA"), "");
    assertEquals(new File("RooT", "PageA"), levelA.getFileSystemPath());
    assertTrue(fileSystem.exists(new File("RooT", "PageA")));
  }

  @Test
  public void testTwoLevel() throws Exception {
    WikiPage levelA = WikiPageUtil.addPage(root, PathParser.parse("PageA"));
    WikiPage page = WikiPageUtil.addPage(levelA, PathParser.parse("PageB"));
    page.commit(page.getData());
    assertTrue(fileSystem.exists(new File(new File("RooT", "PageA"), "PageB")));
  }

  @Test
  public void testContent() throws Exception {
    WikiPagePath rootPath = PathParser.parse("root");
    assertEquals("", root.getPageCrawler().getPage(rootPath).getData().getContent());
    WikiPageUtil.addPage(root, PathParser.parse("AaAa"), "A content");
    assertEquals("A content", root.getChildPage("AaAa").getData().getContent());
    WikiPagePath bPath = PathParser.parse("AaAa.BbBb");
    WikiPageUtil.addPage(root, bPath, "B content");
    assertEquals("B content", root.getPageCrawler().getPage(bPath).getData().getContent());
  }

  @Test
  public void testBigContent() throws Exception {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < 1000; i++)
      buffer.append("abcdefghijklmnopqrstuvwxyz");
    WikiPageUtil.addPage(root, PathParser.parse("BigPage"), buffer.toString());
    String content = root.getChildPage("BigPage").getData().getContent();
    assertTrue(buffer.toString().equals(content));
  }

  @Test
  public void testPageExists() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("AaAa"), "A content");
    assertTrue(root.hasChildPage("AaAa"));
  }

  @Test
  public void testGetChidren() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("AaAa"), "A content");
    WikiPageUtil.addPage(root, PathParser.parse("BbBb"), "B content");
    WikiPageUtil.addPage(root, PathParser.parse("c"), "C content");
    fileSystem.makeDirectory(new File("RooT", ".someOtherDir"));
    fileSystem.makeDirectory(new File("root", "someOther.SubDir"));
    List<WikiPage> children = root.getChildren();
    assertEquals(3, children.size());
    for (WikiPage child : children) {
      String name = child.getName();
      boolean isOk = "AaAa".equals(name) || "BbBb".equals(name) || "c".equals(name);
      assertTrue("WikiPAge is not a valid one: " + name, isOk);
    }
  }

  @Test
  public void testRemovePage() throws Exception {
    WikiPage levelOne = WikiPageUtil.addPage(root, PathParser.parse("LevelOne"));
    levelOne.commit(levelOne.getData());
    WikiPage levelTwo = WikiPageUtil.addPage(levelOne, PathParser.parse("LevelTwo"));
    levelTwo.remove();
    File fileOne = new File("RooT", "LevelOne");
    File fileTwo = new File(new File("RooT", "LevelOne"), "LevelTwo");
    assertTrue(fileSystem.exists(fileOne));
    assertFalse(fileSystem.exists(fileTwo));
  }

  @Test
  public void testDelTree() throws Exception {
    WikiPage levelOne = WikiPageUtil.addPage(root, PathParser.parse("LevelOne"));
    WikiPage levelTwo = WikiPageUtil.addPage(levelOne, PathParser.parse("LevelTwo"));
    levelOne.commit(levelOne.getData());
    levelTwo.commit(levelTwo.getData());
    File childOne = new File("RooT", "LevelOne");
    File childTwo = new File(new File("RooT", "LevelOne"), "LevelTwo");
    assertTrue(fileSystem.exists(childOne));
    root.getChildPage("LevelOne").remove();
    assertFalse(fileSystem.exists(childTwo));
    assertFalse(fileSystem.exists(childOne));
  }

  @Test
  public void testThatExamplesAtEndOfNameSetsSuiteProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageExamples"));
    PageData data = page.getData();
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatExampleAtBeginningOfNameSetsTestProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ExamplePageExample"));
    PageData data = page.getData();
    assertTrue(data.hasAttribute(TEST.toString()));
  }

  @Test
  public void testThatExampleAtEndOfNameSetsTestProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageExample"));
    PageData data = page.getData();
    assertTrue(data.hasAttribute(TEST.toString()));
  }

  @Test
  public void testThatSuiteAtBeginningOfNameSetsSuiteProperty() throws Exception {
    WikiPage suitePage1 = WikiPageUtil.addPage(root, PathParser.parse("SuitePage"));
    PageData data = suitePage1.getData();
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatSuiteAtEndOfNameSetsSuiteProperty() throws Exception {
    WikiPage suitePage2 = WikiPageUtil.addPage(root, PathParser.parse("PageSuite"));
    PageData data = suitePage2.getData();
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatTestAtBeginningOfNameSetsTestProperty() throws Exception {
    WikiPage testPage1 = WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
    PageData data = testPage1.getData();
    assertTrue(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatTestAtEndOfNameSetsTestProperty() throws Exception {
    WikiPage testPage2 = WikiPageUtil.addPage(root, PathParser.parse("PageTest"));
    PageData data = testPage2.getData();
    assertTrue(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testDefaultAttributesForNormalPageNames() throws Exception {
    WikiPage normalPage = WikiPageUtil.addPage(root, PathParser.parse("NormalPage"));
    PageData data = normalPage.getData();
    assertTrue(data.hasAttribute(EDIT));
    assertTrue(data.hasAttribute(SEARCH));
    assertTrue(data.hasAttribute(VERSIONS));
    assertTrue(data.hasAttribute(FILES));
    assertFalse(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testDefaultAttributesForSuitePageNames() throws Exception {
    WikiPage suitePage3 = WikiPageUtil.addPage(root, PathParser.parse("TestPageSuite"));
    PageData data = suitePage3.getData();
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testDefaultAttributesForSuiteSetUpPageNames() throws Exception {
    WikiPage suiteSetupPage = WikiPageUtil.addPage(root, PathParser.parse(SUITE_SETUP_NAME));
    PageData data = suiteSetupPage.getData();
    assertFalse(data.hasAttribute(SUITE.toString()));
  }


  @Test
  public void testDefaultAttributesForSuiteTearDownPageNames() throws Exception {
    WikiPage suiteTearDownPage = WikiPageUtil.addPage(root, PathParser.parse(SUITE_TEARDOWN_NAME));
    PageData data = suiteTearDownPage.getData();
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testPersistentAttributes() throws Exception {
    WikiPage createdPage = WikiPageUtil.addPage(root, PathParser.parse("FrontPage"));
    PageData data = createdPage.getData();
    data.setAttribute("Test", "true");
    data.setAttribute("Search", "true");
    createdPage.commit(data);
    assertTrue(data.hasAttribute("Test"));
    assertTrue(data.hasAttribute("Search"));
    WikiPage page = root.getChildPage("FrontPage");
    assertTrue(page.getData().hasAttribute("Test"));
    assertTrue(page.getData().hasAttribute("Search"));
  }

  @Test
  public void testCanFindExistingPages() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("FrontPage"), "front page");
    WikiPage newRoot = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem)).makePage(new File("RooT"), "RooT", null, new SystemVariableSource());
    assertNotNull(newRoot.getChildPage("FrontPage"));
  }

  @Test
  public void testGetPath() throws Exception {
    assertEquals(new File("RooT"), ((FileBasedWikiPage) root).getFileSystemPath());
  }

  @Test
  public void testLastModifiedTime() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "some text");
    page.commit(page.getData());
    long now = Clock.currentTimeInMillis();
    Date lastModified = page.getData().getProperties().getLastModificationTime();
    assertTrue(now - lastModified.getTime() <= 5000);
  }

  @Test
  public void testUnicodeCharacters() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "\uba80\uba81\uba82\uba83");
    PageData data = page.getData();
    assertEquals("\uba80\uba81\uba82\uba83", data.getContent());
  }

  @Test
  public void testLoadChildrenWhenPageIsDeletedManualy() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
    page.getChildren();
    FileUtil.deleteFileSystemDirectory(((FileBasedWikiPage) page).getFileSystemPath());
    try {
      page.getChildren();
    } catch (Exception e) {
      fail("No Exception should be thrown");
    }
  }
}
