// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.List;

import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class SymbolicPageTest {
  private WikiPage root;
  private BaseWikiPage pageOne;
  private WikiPage pageTwo;
  private SymbolicPage symPage;
  private String pageOnePath = "PageOne";
  private String pageTwoPath = "PageTwo";
  private String pageTwoContent = "page two";
  private WikiPage externalRoot;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    String pageOneContent = "page one";
    pageOne = (BaseWikiPage) WikiPageUtil.addPage(root, PathParser.parse(pageOnePath), pageOneContent);
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse(pageTwoPath), pageTwoContent);
    symPage = new SymbolicPage("SymPage", pageTwo, pageOne);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  @Test
  public void testCreation() throws Exception {
    assertEquals("SymPage", symPage.getName());
  }

  @Test
  public void testLinkage() throws Exception {
    assertSame(pageTwo, symPage.getRealPage());
  }

  @Test
  public void testInternalData() throws Exception {
    PageData data = symPage.getData();
    assertEquals(pageTwoContent, data.getContent());
  }

  @Test
  public void testCommitInternal() throws Exception {
    commitNewContent(symPage);

    PageData data = pageTwo.getData();
    assertEquals("new content", data.getContent());

    data = symPage.getData();
    assertEquals("new content", data.getContent());
  }

  @Test
  public void testGetChild() throws Exception {
    WikiPage childPage = WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), "child page");
    WikiPage page = symPage.getChildPage("ChildPage");
    assertNotNull(page);
    assertEquals(SymbolicPage.class, page.getClass());
    SymbolicPage symChild = (SymbolicPage) page;
    assertEquals(childPage, symChild.getRealPage());
  }

  @Test
  public void testGetChildren() throws Exception {
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildOne"), "child one");
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildTwo"), "child two");
    List<?> children = symPage.getChildren();
    assertEquals(2, children.size());
    assertEquals(SymbolicPage.class, children.get(0).getClass());
    assertEquals(SymbolicPage.class, children.get(1).getClass());
  }

  @Test
  public void testCyclicSymbolicLinks() throws Exception {
    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymOne", pageTwoPath);
    pageOne.commit(data);

    data = pageTwo.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymTwo", pageOnePath);
    pageTwo.commit(data);
    PageCrawler pageCrawler = root.getPageCrawler();
    WikiPage deepPage = pageCrawler.getPage(PathParser.parse(pageOnePath + ".SymOne.SymTwo.SymOne.SymTwo.SymOne"));
    List<?> children = deepPage.getChildren();
    assertEquals(1, children.size());

    deepPage = pageCrawler.getPage(PathParser.parse(pageTwoPath + ".SymTwo.SymOne.SymTwo.SymOne.SymTwo"));
    children = deepPage.getChildren();
    assertEquals(1, children.size());
  }

  @Test
  public void nestedSymbolicLinksShouldKeepTheRightPath() {
    String pageThreePath = "PageThree";
    String pageThreeContent = "page three";
    WikiPage pageThree = WikiPageUtil.addPage(root, PathParser.parse(pageThreePath), pageThreeContent);

    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymOne", pageTwoPath);
    pageOne.commit(data);

    data = pageTwo.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymTwo", pageThreePath);
    pageTwo.commit(data);
    PageCrawler pageCrawler = root.getPageCrawler();
    WikiPagePath fullPath = PathParser.parse(pageOnePath + ".SymOne.SymTwo");
    WikiPage deepPage = pageCrawler.getPage(fullPath);

    assertEquals(deepPage.getPageCrawler().getFullPath(), fullPath);
  }

  @Test
  public void testSymbolicPageUsingExternalDirectory() throws Exception {
    CreateExternalRoot();

    assertEquals(2, symPage.getChildren().size());

    WikiPage symPageOne = symPage.getChildPage("ExternalPageOne");
    assertNotNull(symPageOne);
    assertEquals("external page one", symPageOne.getData().getContent());

    WikiPage symPageTwo = symPage.getChildPage("ExternalPageTwo");
    assertNotNull(symPageTwo);
    assertEquals("external page two", symPageTwo.getData().getContent());

    WikiPage symChild = symPageOne.getChildPage("ExternalChild");
    assertNotNull(symChild);
    assertEquals("external child", symChild.getData().getContent());
  }

  private void CreateExternalRoot() throws Exception {
    FileUtil.createDir("testDir");
    FileUtil.createDir("testDir/ExternalRoot");
    externalRoot = new FileSystemPageFactory().makePage(new File("testDir/ExternalRoot"), "ExternalRoot", null, new SystemVariableSource());
    WikiPage externalPageOne = WikiPageUtil.addPage(externalRoot, PathParser.parse("ExternalPageOne"), "external page one");
    WikiPageUtil.addPage(externalPageOne, PathParser.parse("ExternalChild"), "external child");
    WikiPageUtil.addPage(externalRoot, PathParser.parse("ExternalPageTwo"), "external page two");

    symPage = new SymbolicPage("SymPage", externalRoot, pageOne);
  }

  @Test
  public void testCommittingToExternalRoot() throws Exception {
    CreateExternalRoot();

    commitNewContent(symPage);

    assertEquals("new content", externalRoot.getData().getContent());

    commitNewContent(symPage.getChildPage("ExternalPageOne"));

    assertEquals("new content", externalRoot.getChildPage("ExternalPageOne").getData().getContent());
  }

  private void commitNewContent(WikiPage wikiPage) throws Exception {
    PageData data = wikiPage.getData();
    data.setContent("new content");
    wikiPage.commit(data);
  }
}
