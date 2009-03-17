// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import util.FileUtil;

public class BaseWikiPageTest extends TestCase {
  private BaseWikiPage root;
  private WikiPage linkingPage;

  public void setUp() throws Exception {
    root = (BaseWikiPage) InMemoryPage.makeRoot("RooT");
    root.addChildPage("LinkedPage");
    linkingPage = root.addChildPage("LinkingPage");
    linkingPage.addChildPage("ChildPage");
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  public void testGetChildrenUsesSymbolicPages() throws Exception {
    createLink("LinkedPage");

    List<WikiPage> children = linkingPage.getChildren();
    assertEquals(2, children.size());
    assertEquals("ChildPage", children.get(0).getName());

    checkSymbolicPage(children.get(1));
  }

  public void testGetChildUsesSymbolicPages() throws Exception {
    createLink("LinkedPage");
    checkSymbolicPage(linkingPage.getChildPage("SymLink"));
  }

  public void testCanCreateSymLinksToExternalDirectories() throws Exception {
    FileUtil.createDir("testDir");
    FileUtil.createDir("testDir/ExternalRoot");

    createLink("file://testDir/ExternalRoot");

    checkExternalLink();
  }

  private void checkExternalLink() throws Exception {
    WikiPage symPage = linkingPage.getChildPage("SymLink");
    assertNotNull(symPage);
    assertEquals(SymbolicPage.class, symPage.getClass());

    WikiPage realPage = ((SymbolicPage) symPage).getRealPage();
    assertEquals(FileSystemPage.class, realPage.getClass());

    assertEquals("testDir/ExternalRoot", ((FileSystemPage) realPage).getFileSystemPath());
    assertEquals("ExternalRoot", ((FileSystemPage) realPage).getName());
  }

  public void testExternalSymbolicLinkToNewDirectory() throws Exception {
    FileUtil.createDir("testDir");
    createLink("file://testDir/ExternalRoot");

    checkExternalLink();
    assertTrue(new File("testDir/ExternalRoot").exists());
  }

  private void createLink(String linkedPagePath) throws Exception {
    PageData data = linkingPage.getData();
    WikiPageProperties properties = data.getProperties();
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
}
