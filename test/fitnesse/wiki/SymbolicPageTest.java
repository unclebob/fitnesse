// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SymbolicPageTest {
  private WikiPage root;
  private WikiPage pageOne;
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
    pageOne = WikiPageUtil.addPage(root, PathParser.parse(pageOnePath), pageOneContent);
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse(pageTwoPath), pageTwoContent);
    symPage = new SymbolicPage("SymPage", pageTwo, pageOne);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  @Test
  public void testCreation() {
    assertEquals("SymPage", symPage.getName());
  }

  @Test
  public void testLinkage() {
    assertSame(pageTwo, symPage.getRealPage());
  }

  @Test
  public void doesNotEqualAnotherSymbolicPageWithOtherNameAndTheSameRealPage() {
    assertNotEquals(symPage, new SymbolicPage("SymPage2", pageTwo, pageOne));
  }

  @Test
  public void equalAnotherSymbolicPageWithTheSameNameAndRealPage() {
    assertEquals(symPage, new SymbolicPage(symPage.getName(), pageTwo, pageOne));
  }

  @Test
  public void equalsItSelf() {
    assertEquals(symPage, symPage);
    assertEquals(symPage.hashCode(), symPage.hashCode());
  }

  @Test
  public void equalsRealPage() {
    assertEquals(symPage, pageTwo);
    assertEquals(pageTwo, symPage);
    assertEquals(symPage.hashCode(), pageTwo.hashCode());
    assertEquals(pageTwo.hashCode(), symPage.hashCode());
  }

  @Test
  public void doesNotEqualAnotherSymbolicPageWithDifferentRealPage() {
    WikiPage childPage = WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildOne"), "child one");
    assertNotEquals(symPage, new SymbolicPage(symPage.getName(), childPage, pageOne));
  }

  @Test
  public void doesNotEqualAnotherSymbolicPageWithDifferentExternalRoot() {
    createExternalRoot();
    
    FileUtil.createDir("testDir/ExternalRoot2");
    WikiPage externalRoot2 = new FileSystemPageFactory().makePage(new File("testDir/ExternalRoot2"), "ExternalRoot2", null, new SystemVariableSource());
    WikiPage symPage2 = new SymbolicPage(symPage.getName(), externalRoot2, pageOne);
  
    assertNotEquals(symPage, symPage2);
  }
  
  @Test
  public void testInternalData() {
    PageData data = symPage.getData();
    assertEquals(pageTwoContent, data.getContent());
  }

  @Test
  public void testCommitInternal() {
    commitNewContent(symPage);

    PageData data = pageTwo.getData();
    assertEquals("new content", data.getContent());

    data = symPage.getData();
    assertEquals("new content", data.getContent());
  }

  @Test
  public void testGetChild() {
    WikiPage childPage = WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), "child page");
    WikiPage page = symPage.getChildPage("ChildPage");
    assertNotNull(page);
    assertEquals(SymbolicPage.class, page.getClass());
    SymbolicPage symChild = (SymbolicPage) page;
    assertEquals(childPage, symChild.getRealPage());
  }

  @Test
  public void canDetermineIfOriginalPageIsWikiTextPage() {
    WikiPage symSymPage = new SymbolicPage("SymSymPage", symPage, pageOne);
    assertTrue(SymbolicPage.containsWikitext(symSymPage));
  }

  @Test
  public void canDetermineIfOriginalPageIsNotWikiTextPage() {
    WikiPage wikiPage = mock(WikiPage.class);
    WikiPage symPage = new SymbolicPage("SymPage", wikiPage, pageOne);
    WikiPage symSymPage = new SymbolicPage("SymSymPage", symPage, pageOne);
    assertFalse(SymbolicPage.containsWikitext(symSymPage));
  }

  @Test
  public void testGetChildren() {
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildOne"), "child one");
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildTwo"), "child two");
    List<?> children = symPage.getChildren();
    assertEquals(2, children.size());
    assertEquals(SymbolicPage.class, children.get(0).getClass());
    assertEquals(SymbolicPage.class, children.get(1).getClass());
  }

  @Test
  public void canCreateMultipleLinksToSamePage() {
    PageData data = pageOne.getData();
    WikiPageProperty suiteProperty = data.getProperties().set(SymbolicPage.PROPERTY_NAME);
    suiteProperty.set("SymOne", pageTwoPath);
    suiteProperty.set("SymTwo", pageTwoPath);
    pageOne.commit(data);

    PageCrawler pageCrawler = root.getPageCrawler();
    WikiPage deepPage = pageCrawler.getPage(PathParser.parse(pageOnePath));
    List<WikiPage> children = deepPage.getChildren();
    assertEquals(2, children.size());
  }

  @Test
  public void wikitextPageShouldReadParentVariable() {
    WikiPage pageWithVariable = WikiPageUtil.addPage(root, PathParser.parse("ChildOne"), "!define Variable {my variable}");
    WikiPage symPage = new SymbolicPage("SymPage", pageOne, pageWithVariable);

    assertEquals("my variable", symPage.getVariable("Variable"));
  }

  @Test
  public void nonWikitextPageShouldReadParentVariable() {
    WikiPage pageWithVariable = WikiPageUtil.addPage(root, PathParser.parse("ChildOne"), "!define Variable {my variable}");
    WikiPage realPage = mock(WikiPage.class);
    WikiPage symPage = new SymbolicPage("SymPage", realPage, pageWithVariable);

    assertEquals("my variable", symPage.getVariable("Variable"));
  }

  @Test
  public void testCyclicSymbolicLinks() {
    createCycle();
    PageCrawler pageCrawler = root.getPageCrawler();

    WikiPage deepPage = pageCrawler.getPage(PathParser.parse(pageOnePath + ".SymOne.SymTwo.SymOne.SymTwo.SymOne"));
    assertNull(deepPage);

  }

  @Test
  public void cyclicSymbolicLinkDisplaysAMessage() {
    createCycle();
    PageCrawler pageCrawler = root.getPageCrawler();
    WikiPage deepPage = pageCrawler.getPage(PathParser.parse(pageTwoPath + ".SymTwo.SymOne"));
    List<WikiPage> children = deepPage.getChildren();
    assertEquals(0, children.size());
    assertEquals("<em>Short circuit! This page references PageTwo, which is already one of the parent pages of this page.</em>", deepPage.getHtml());
  }

  private void createCycle() {
    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymOne", pageTwoPath);
    pageOne.commit(data);

    data = pageTwo.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymTwo", pageOnePath);
    pageTwo.commit(data);
  }

  @Test
  public void nestedSymbolicLinksShouldKeepTheRightPath() {
    String pageThreePath = "PageThree";
    String pageThreeContent = "page three";
    WikiPageUtil.addPage(root, PathParser.parse(pageThreePath), pageThreeContent);

    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymOne", pageTwoPath);
    pageOne.commit(data);

    data = pageTwo.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymTwo", pageThreePath);
    pageTwo.commit(data);
    PageCrawler pageCrawler = root.getPageCrawler();
    WikiPagePath fullPath = PathParser.parse(pageOnePath + ".SymOne.SymTwo");
    WikiPage deepPage = pageCrawler.getPage(fullPath);

    assertEquals(deepPage.getFullPath(), fullPath);
  }

  @Test
  public void testSymbolicPageUsingExternalDirectory() {
    createExternalRoot();

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

  private void createExternalRoot() {
    FileUtil.createDir("testDir");
    FileUtil.createDir("testDir/ExternalRoot");
    externalRoot = new FileSystemPageFactory().makePage(new File("testDir/ExternalRoot"), "ExternalRoot", null, new SystemVariableSource());
    WikiPage externalPageOne = WikiPageUtil.addPage(externalRoot, PathParser.parse("ExternalPageOne"), "external page one");
    WikiPageUtil.addPage(externalPageOne, PathParser.parse("ExternalChild"), "external child");
    WikiPageUtil.addPage(externalRoot, PathParser.parse("ExternalPageTwo"), "external page two");

    symPage = new SymbolicPage("SymPage", externalRoot, pageOne);
  }

  @Test
  public void testCommittingToExternalRoot() {
    createExternalRoot();

    commitNewContent(symPage);

    assertEquals("new content", externalRoot.getData().getContent());

    commitNewContent(symPage.getChildPage("ExternalPageOne"));

    assertEquals("new content", externalRoot.getChildPage("ExternalPageOne").getData().getContent());
  }

  private void commitNewContent(WikiPage wikiPage) {
    PageData data = wikiPage.getData();
    data.setContent("new content");
    wikiPage.commit(data);
  }
}
