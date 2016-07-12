// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.fs.*;
import util.FileUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BaseWikiPageTest {
  private WikiPage linkingPage;
  private WikiPage root;
  private MemoryFileSystem fileSystem;

  @Before
  public void setUp() throws Exception {
    fileSystem = new MemoryFileSystem();
    root = InMemoryPage.makeRoot("RooT", fileSystem);
    WikiPageUtil.addPage(root, PathParser.parse("LinkedPage"), "");
    linkingPage = WikiPageUtil.addPage(root, PathParser.parse("LinkingPage"), "");
    WikiPageUtil.addPage(linkingPage, PathParser.parse("ChildPage"), "");
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  @Test
  public void testGetChildrenUsesSymbolicPages() throws Exception {
    createLink("LinkedPage");

    List<WikiPage> children = linkingPage.getChildren();
    assertEquals(2, children.size());
    assertEquals("ChildPage", children.get(0).getName());

    checkSymbolicPage(children.get(1));
  }

  @Test
  public void testGetChildUsesSymbolicPages() throws Exception {
    createLink("LinkedPage");
    checkSymbolicPage(linkingPage.getChildPage("SymLink"));
  }


  @Test
  public void testThatSpecialCharsAreNotEscapedTwice() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SpecialChars"), "<b>");
    String html = page.getHtml();
    assertEquals("&lt;b&gt;", html);
  }

  @Test
  public void testCanCreateSymLinksToRelativeExternalDirectories() throws Exception {
    fileSystem.makeDirectory(new File("testDir").getCanonicalFile());
    fileSystem.makeDirectory(new File("testDir/ExternalRoot").getCanonicalFile());

    createLink("file://testDir/ExternalRoot");

    WikiPage symPage = linkingPage.getChildPage("SymLink");
    assertNotNull(symPage);
    assertEquals(SymbolicPage.class, symPage.getClass());

    WikiPage realPage = ((SymbolicPage) symPage).getRealPage();
    assertEquals(WikiFilePage.class, realPage.getClass());

    assertEquals(new File("testDir/ExternalRoot").getCanonicalFile(), ((FileBasedWikiPage) realPage).getFileSystemPath());
    assertEquals("ExternalRoot", realPage.getName());
  }

  private void createLink(String linkedPagePath) throws Exception {
    PageData data = linkingPage.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(SymbolicPage.PROPERTY_NAME);
    properties.getProperty(SymbolicPage.PROPERTY_NAME).set("SymLink", linkedPagePath);
    linkingPage.commit(data);
  }

  private void checkSymbolicPage(Object page) throws Exception {
    assertEquals(SymbolicPage.class, page.getClass());
    SymbolicPage symPage = (SymbolicPage) page;
    assertEquals("SymLink", symPage.getName());
    assertEquals("LinkedPage", symPage.getRealPage().getName());
  }

  @Test
  public void testIsRoot() throws Exception {
    assertTrue(root.isRoot());
    assertFalse(linkingPage.isRoot());
  }


}
