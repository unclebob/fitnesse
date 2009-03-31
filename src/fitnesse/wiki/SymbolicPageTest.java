// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.List;

import junit.framework.TestCase;
import util.FileUtil;

public class SymbolicPageTest extends TestCase {
  private PageCrawler crawler;
  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage pageTwo;
  private SymbolicPage symPage;
  private String pageOnePath = "PageOne";
  private String pageTwoPath = "PageTwo";
  private String pageOneContent = "page one";
  private String pageTwoContent = "page two";
  private WikiPage externalRoot;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    pageOne = crawler.addPage(root, PathParser.parse(pageOnePath), pageOneContent);
    pageTwo = crawler.addPage(root, PathParser.parse(pageTwoPath), pageTwoContent);
    symPage = new SymbolicPage("SymPage", pageTwo, pageOne);
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  public void testCreation() throws Exception {
    assertEquals("SymPage", symPage.getName());
  }

  public void testLinkage() throws Exception {
    assertSame(pageTwo, symPage.getRealPage());
  }

  public void testInternalData() throws Exception {
    PageData data = symPage.getData();
    assertEquals(pageTwoContent, data.getContent());
    assertSame(symPage, data.getWikiPage());
  }

  public void testCommitInternal() throws Exception {
    commitNewContent(symPage);

    PageData data = pageTwo.getData();
    assertEquals("new content", data.getContent());

    data = symPage.getData();
    assertEquals("new content", data.getContent());
  }

  public void testGetChild() throws Exception {
    WikiPage childPage = crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "child page");
    WikiPage page = symPage.getChildPage("ChildPage");
    assertNotNull(page);
    assertEquals(SymbolicPage.class, page.getClass());
    SymbolicPage symChild = (SymbolicPage) page;
    assertSame(childPage, symChild.getRealPage());
  }

  public void testGetChildren() throws Exception {
    crawler.addPage(pageTwo, PathParser.parse("ChildOne"), "child one");
    crawler.addPage(pageTwo, PathParser.parse("ChildTwo"), "child two");
    List<?> children = symPage.getChildren();
    assertEquals(2, children.size());
    assertEquals(SymbolicPage.class, children.get(0).getClass());
    assertEquals(SymbolicPage.class, children.get(1).getClass());
  }

  public void testCyclicSymbolicLinks() throws Exception {
    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymOne", pageTwoPath);
    pageOne.commit(data);

    data = pageTwo.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymTwo", pageOnePath);
    pageTwo.commit(data);

    WikiPage deepPage = crawler.getPage(root, PathParser.parse(pageOnePath + ".SymOne.SymTwo.SymOne.SymTwo.SymOne"));
    List<?> children = deepPage.getChildren();
    assertEquals(1, children.size());

    deepPage = crawler.getPage(root, PathParser.parse(pageTwoPath + ".SymTwo.SymOne.SymTwo.SymOne.SymTwo"));
    children = deepPage.getChildren();
    assertEquals(1, children.size());
  }

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
    externalRoot = new FileSystemPage("testDir/ExternalRoot", "ExternalRoot");
    PageCrawler externalCrawler = externalRoot.getPageCrawler();
    WikiPage externalPageOne = externalCrawler.addPage(externalRoot, PathParser.parse("ExternalPageOne"), "external page one");
    externalCrawler.addPage(externalPageOne, PathParser.parse("ExternalChild"), "external child");
    externalCrawler.addPage(externalRoot, PathParser.parse("ExternalPageTwo"), "external page two");

    symPage = new SymbolicPage("SymPage", externalRoot, pageOne);
  }

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
