// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki.fs;

import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.Clock;
import util.FileUtil;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class FileSystemPageTest {
  private static final String defaultPath = "./teststorage";
  private static final File base = new File(defaultPath);
  private FileSystemPage root;

  @BeforeClass
  public static void initialize() {
    FileUtil.deleteFileSystemDirectory(base);
    FileUtil.deleteFileSystemDirectory("RooT");
  }

  @Before
  public void setUp() throws Exception {
    FileUtil.deleteFileSystemDirectory(base);
    createFileSystemDirectory(base);
    root = new FileSystemPageFactory().makeRootPage(defaultPath, "RooT");
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(base);
    FileUtil.deleteFileSystemDirectory("RooT");
  }

  public static void createFileSystemDirectory(File current) {
    current.mkdir();
  }

  @Test
  public void testCreateBase() throws Exception {
    FileSystemPage levelA = (FileSystemPage) WikiPageUtil.addPage(root, PathParser.parse("PageA"), "");
    assertEquals("./teststorage/RooT/PageA", levelA.getFileSystemPath());
    assertTrue(new File(defaultPath + "/RooT/PageA").exists());
  }

  @Test
  public void testTwoLevel() throws Exception {
    WikiPage levelA = WikiPageUtil.addPage(root, PathParser.parse("PageA"));
    WikiPage page = WikiPageUtil.addPage(levelA, PathParser.parse("PageB"));
    page.commit(page.getData());
    assertTrue(new File(defaultPath + "/RooT/PageA/PageB").exists());
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
    StringBuffer buffer = new StringBuffer();
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
    WikiPageUtil.addPage(root, PathParser.parse("CcCc"), "C content");
    new File(defaultPath + "/root/someOtherDir").mkdir();
    List<WikiPage> children = root.getChildren();
    assertEquals(3, children.size());
    for (WikiPage child : children) {
      String name = child.getName();
      boolean isOk = "AaAa".equals(name) || "BbBb".equals(name) || "CcCc".equals(name);
      assertTrue("WikiPAge is not a valid one: " + name, isOk);
    }
  }

  @Test
  public void testRemovePage() throws Exception {
    WikiPage levelOne = WikiPageUtil.addPage(root, PathParser.parse("LevelOne"));
    levelOne.commit(levelOne.getData());
    WikiPageUtil.addPage(levelOne, PathParser.parse("LevelTwo"));
    levelOne.removeChildPage("LevelTwo");
    File fileOne = new File(defaultPath + "/RooT/LevelOne");
    File fileTwo = new File(defaultPath + "/RooT/LevelOne/LevelTwo");
    assertTrue(fileOne.exists());
    assertFalse(fileTwo.exists());
  }

  @Test
  public void testDelTree() throws Exception {
    WikiPage levelOne = WikiPageUtil.addPage(root, PathParser.parse("LevelOne"));
    WikiPage levelTwo = WikiPageUtil.addPage(levelOne, PathParser.parse("LevelTwo"));
    levelOne.commit(levelOne.getData());
    levelTwo.commit(levelTwo.getData());
    File childOne = new File(defaultPath + "/RooT/LevelOne");
    File childTwo = new File(defaultPath + "/RooT/LevelOne/LevelTwo");
    assertTrue(childOne.exists());
    root.removeChildPage("LevelOne");
    assertFalse(childTwo.exists());
    assertFalse(childOne.exists());
  }

  @Test
  public void testDefaultAttributes() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "something");
    assertTrue(page.getData().hasAttribute("Edit"));
    assertTrue(page.getData().hasAttribute("Search"));
    assertFalse(page.getData().hasAttribute("Test"));
    assertFalse(page.getData().hasAttribute("TestSuite"));
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
    WikiPage newRoot = new FileSystemPageFactory().makeRootPage(defaultPath, "RooT");
    assertNotNull(newRoot.getChildPage("FrontPage"));
  }

  @Test
  public void testGetPath() throws Exception {
    assertEquals(defaultPath + "/RooT", root.getFileSystemPath());
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
    FileUtil.deleteFileSystemDirectory(((FileSystemPage) page).getFileSystemPath());
    try {
      page.getChildren();
    } catch (Exception e) {
      fail("No Exception should be thrown");
    }
  }
}
