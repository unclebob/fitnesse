// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;

import util.RegexTestCase;
import util.XmlUtil;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class WikiImporterTest extends RegexTestCase implements WikiImporterClient {
  public WikiPage pageOne;
  public WikiPage childPageOne;
  public WikiPage pageTwo;
  public WikiPage remoteRoot;
  private WikiImporter importer;
  private LinkedList<WikiPage> imports;
  private LinkedList<Exception> errors;
  public WikiPage localRoot;

  public void setUp() throws Exception {
    createRemoteRoot();
    createLocalRoot();

    FitNesseUtil.startFitnesse(remoteRoot);

    importer = new WikiImporter();
    importer.setWikiImporterClient(this);
    importer.parseUrl("http://localhost:" + FitNesseUtil.port);

    imports = new LinkedList<WikiPage>();
    errors = new LinkedList<Exception>();
  }

  public void createLocalRoot() throws Exception {
    localRoot = InMemoryPage.makeRoot("RooT2");
    pageOne = localRoot.addChildPage("PageOne");
    childPageOne = pageOne.addChildPage("ChildOne");
    pageTwo = localRoot.addChildPage("PageTwo");
  }

  public WikiPage createRemoteRoot() throws Exception {
    remoteRoot = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = remoteRoot.getPageCrawler();
    crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
    crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
    crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");
    return remoteRoot;
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  public void testEnterChildPage() throws Exception {
    importer.enterChildPage(pageOne, new Date());

    PageData data = pageOne.getData();
    assertEquals("page one", data.getContent());
  }

  public void testChildPageAdded() throws Exception {
    importer.enterChildPage(pageOne, new Date());
    importer.enterChildPage(childPageOne, new Date());

    PageData data = childPageOne.getData();
    assertEquals("child one", data.getContent());
  }

  public void testEnterChildPageWhenRemotePageNotModified() throws Exception {
    importer.enterChildPage(pageOne, new Date());
    importer.exitPage();

    PageData data = pageOne.getData();
    data.setContent("new content");
    pageOne.commit(data);

    importer.enterChildPage(pageOne, new Date(0));

    assertEquals("new content", pageOne.getData().getContent());
  }

  public void testExiting() throws Exception {
    importer.enterChildPage(pageOne, new Date());
    importer.enterChildPage(childPageOne, new Date());
    importer.exitPage();
    importer.exitPage();
    importer.enterChildPage(pageTwo, new Date());

    PageData data = pageTwo.getData();
    assertEquals("page two", data.getContent());
  }

  public void testGetPageTree() throws Exception {
    Document doc = importer.getPageTree();
    assertNotNull(doc);
    String xml = XmlUtil.xmlAsString(doc);

    assertSubString("PageOne", xml);
    assertSubString("PageTwo", xml);
  }

  public void testUrlParsing() throws Exception {
    testUrlParsing("http://mysite.com", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com/", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com:8080/", "mysite.com", 8080, "");
    testUrlParsing("http://mysite.com:8080", "mysite.com", 8080, "");
    testUrlParsing("http://mysite.com:80/", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com/PageOne", "mysite.com", 80, "PageOne");
    testUrlParsing("http://mysite.com/PageOne.ChildOne", "mysite.com", 80, "PageOne.ChildOne");
  }

  private void testUrlParsing(String url, String host, int port, String path) throws Exception {
    importer.parseUrl(url);
    assertEquals(host, importer.getRemoteHostname());
    assertEquals(port, importer.getRemotePort());
    assertEquals(path, PathParser.render(importer.getRemotePath()));
  }

  public void testParsingBadUrl() throws Exception {
    try {
      importer.parseUrl("blah");
      fail("should have exception");
    }
    catch (Exception e) {
      assertEquals("blah is not a valid URL.", e.getMessage());
    }
  }

  public void testParsingUrlWithNonWikiWord() throws Exception {
    try {
      importer.parseUrl("http://blah.com/notawikiword");
      fail("should throw exception");
    }
    catch (Exception e) {
      assertEquals("The URL's resource path, notawikiword, is not a valid WikiWord.", e.getMessage());
    }
  }

  public void testImportingWiki() throws Exception {
    localRoot = InMemoryPage.makeRoot("LocalRoot");
    importer.importWiki(localRoot);

    assertEquals(2, localRoot.getChildren().size());
    assertEquals(3, imports.size());
    assertEquals(0, errors.size());
  }

  public void testFindsOrphansOnLocalWiki() throws Exception {
    performImportWithExtraLocalPages();

    List<WikiPagePath> orphans = importer.getOrphans();
    assertEquals(3, orphans.size());
    assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageThree")));
    assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageOne").addNameToEnd("ChildTwo")));
    assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageOne").addNameToEnd("ChildOne").addNameToEnd("GrandChildOne")));
    assertFalse(orphans.contains(new WikiPagePath().addNameToEnd("PageThatDoesntImport")));
    assertFalse(orphans.contains(new WikiPagePath().addNameToEnd("OtherImportRoot")));
  }

  private void performImportWithExtraLocalPages() throws Exception {
    addLocalPageWithImportProperty(localRoot, "PageThree", false);
    addLocalPageWithImportProperty(pageOne, "ChildTwo", false);
    addLocalPageWithImportProperty(childPageOne, "GrandChildOne", false);
    localRoot.addChildPage("PageThatDoesntImport");
    addLocalPageWithImportProperty(localRoot, "OtherImportRoot", true);

    importer.importWiki(localRoot);
  }

  public void testOrphansAreRemoved() throws Exception {
    performImportWithExtraLocalPages();

    assertFalse(localRoot.hasChildPage("PageThree"));
    assertFalse(pageOne.hasChildPage("ChildTwo"));
    assertFalse(childPageOne.hasChildPage("GrandChildOne"));

    assertTrue(localRoot.hasChildPage("PageThatDoesntImport"));
    assertTrue(localRoot.hasChildPage("OtherImportRoot"));
  }

  public void testWholeTreeOrphaned() throws Exception {
    importer.importWiki(localRoot);

    remoteRoot.removeChildPage("PageOne");

    importer.importWiki(localRoot);

    assertFalse(localRoot.hasChildPage("PageOne"));
  }

  public void testContextIsNotOrphanWhenUpdatingNonRoot() throws Exception {
    addLocalPageWithImportProperty(localRoot, "PageOne", false);
    importer.parseUrl("http://localhost:" + FitNesseUtil.port + "/PageOne");

    importer.importWiki(localRoot.getChildPage("PageOne"));

    assertEquals(0, importer.getOrphans().size());
  }

  public void testAutoUpdatePropertySetOnRoot() throws Exception {
    addLocalPageWithImportProperty(localRoot, "PageOne", false);
    importer.parseUrl("http://localhost:" + FitNesseUtil.port + "/PageOne");
    importer.setAutoUpdateSetting(true);
    WikiPage importedPage = localRoot.getChildPage("PageOne");
    importer.importWiki(importedPage);

    WikiImportProperty importProp = WikiImportProperty.createFrom(importedPage.getData().getProperties());
    assertTrue(importProp.isAutoUpdate());

    importer.setAutoUpdateSetting(false);
    importer.importWiki(importedPage);

    importProp = WikiImportProperty.createFrom(importedPage.getData().getProperties());
    assertFalse(importProp.isAutoUpdate());
  }

  public void testAutoUpdate_NewPage() throws Exception {
    importer.setAutoUpdateSetting(true);
    importer.enterChildPage(pageOne, new Date());

    WikiImportProperty importProps = WikiImportProperty.createFrom(pageOne.getData().getProperties());
    assertTrue(importProps.isAutoUpdate());
  }

  public void testAutoUpdateWhenRemotePageNotModified() throws Exception {
    importer.enterChildPage(pageOne, new Date());
    importer.exitPage();

    PageData data = pageOne.getData();
    data.setContent("new content");
    pageOne.commit(data);

    importer.setAutoUpdateSetting(true);
    importer.enterChildPage(pageOne, new Date(0));

    WikiImportProperty importProps = WikiImportProperty.createFrom(pageOne.getData().getProperties());
    assertTrue(importProps.isAutoUpdate());
  }

  private WikiPage addLocalPageWithImportProperty(WikiPage parentPage, String pageName, boolean isRoot) throws Exception {
    WikiPage page = parentPage.addChildPage(pageName);
    PageData data = page.getData();

    WikiPagePath pagePath = localRoot.getPageCrawler().getFullPath(page);
    WikiImportProperty importProps = new WikiImportProperty("http://localhost:" + FitNesseUtil.port + "/" + PathParser.render(pagePath));
    if (isRoot)
      importProps.setRoot(true);
    importProps.addTo(data.getProperties());
    page.commit(data);

    return page;
  }

  public void pageImported(WikiPage page) {
    imports.add(page);
  }

  public void pageImportError(WikiPage page, Exception e) {
    errors.add(e);
  }
}
