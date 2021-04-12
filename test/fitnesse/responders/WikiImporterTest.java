// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.Clock;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static util.RegexTestCase.assertSubString;

public class WikiImporterTest implements WikiImporterClient {
  public WikiPage pageOne;
  public WikiPage childPageOne;
  public WikiPage pageTwo;
  private WikiImporter importer;
  private LinkedList<WikiPage> imports;
  private LinkedList<Exception> errors;
  private WikiPage remoteRoot;
  private WikiPage localRoot;
  public FitNesseContext localContext;
  public FitNesseContext remoteContext;

  @Before
  public void setUp() throws Exception {
    createRemoteRoot();
    createLocalRoot();

    FitNesseUtil.startFitnesseWithContext(remoteContext);

    importer = new WikiImporter();
    importer.setWikiImporterClient(this);
    importer.parseUrl("http://localhost:" + FitNesseUtil.PORT);

    imports = new LinkedList<>();
    errors = new LinkedList<>();
  }

  public FitNesseContext createLocalRoot() throws Exception {
    localContext = FitNesseUtil.makeTestContext();
    localRoot = localContext.getRootPage();
    pageOne = WikiPageUtil.addPage(localRoot, PathParser.parse("PageOne"), "");
    childPageOne = WikiPageUtil.addPage(pageOne, PathParser.parse("ChildOne"), "");
    pageTwo = WikiPageUtil.addPage(localRoot, PathParser.parse("PageTwo"), "");
    return localContext;
  }

  public FitNesseContext createRemoteRoot(Authenticator authenticator) {
    remoteContext = FitNesseUtil.makeTestContext(authenticator);
    remoteRoot = remoteContext.getRootPage();
    WikiPageUtil.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
    WikiPageUtil.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
    WikiPageUtil.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");
    return remoteContext;
  }

  public FitNesseContext createRemoteRoot() throws Exception {
    return createRemoteRoot(new PromiscuousAuthenticator());
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  @Test
  public void testEnterChildPage() throws Exception {
    importer.enterChildPage(pageOne, Clock.currentDate());

    PageData data = pageOne.getData();
    assertEquals("page one", data.getContent());
  }

  @Test
  public void testChildPageAdded() throws Exception {
    importer.enterChildPage(pageOne, Clock.currentDate());
    importer.enterChildPage(childPageOne, Clock.currentDate());

    PageData data = childPageOne.getData();
    assertEquals("child one", data.getContent());
  }

  @Test
  public void testEnterChildPageWhenRemotePageNotModified() throws Exception {
    importer.enterChildPage(pageOne, Clock.currentDate());
    importer.exitPage();

    PageData data = pageOne.getData();
    data.setContent("new content");
    pageOne.commit(data);

    importer.enterChildPage(pageOne, new Date(0));

    assertEquals("new content", pageOne.getData().getContent());
  }

  @Test
  public void testExiting() throws Exception {
    importer.enterChildPage(pageOne, Clock.currentDate());
    importer.enterChildPage(childPageOne, Clock.currentDate());
    importer.exitPage();
    importer.exitPage();
    importer.enterChildPage(pageTwo, Clock.currentDate());

    PageData data = pageTwo.getData();
    assertEquals("page two", data.getContent());
  }

  @Test
  public void testGetPageTree() throws Exception {
    Document doc = importer.getPageTree();
    assertNotNull(doc);
    String xml = XmlUtil.xmlAsString(doc);

    assertSubString("PageOne", xml);
    assertSubString("PageTwo", xml);
  }

  @Test
  public void testUrlParsingHttp() throws Exception {
    testUrlParsing("http://mysite.com", "http", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com/", "http", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com:8080/", "http", "mysite.com", 8080, "");
    testUrlParsing("http://mysite.com:8080", "http", "mysite.com", 8080, "");
    testUrlParsing("http://mysite.com:80/", "http", "mysite.com", 80, "");
    testUrlParsing("http://mysite.com/PageOne", "http", "mysite.com", 80, "PageOne");
    testUrlParsing("http://mysite.com/PageOne.ChildOne", "http", "mysite.com", 80, "PageOne.ChildOne");
  }

  @Test
  public void testUrlParsingHttps() throws Exception {
    testUrlParsing("https://mysite.com", "https", "mysite.com", 443, "");
    testUrlParsing("https://mysite.com/", "https", "mysite.com", 443, "");
    testUrlParsing("https://mysite.com:8080/", "https", "mysite.com", 8080, "");
    testUrlParsing("https://mysite.com:8080", "https", "mysite.com", 8080, "");
    testUrlParsing("https://mysite.com:80/", "https", "mysite.com", 80, "");
    testUrlParsing("https://mysite.com/PageOne", "https", "mysite.com", 443, "PageOne");
    testUrlParsing("https://mysite.com/PageOne.ChildOne", "https", "mysite.com", 443, "PageOne.ChildOne");
    testUrlParsing("https://mysite.com:377/PageOne.ChildOne", "https", "mysite.com", 377, "PageOne.ChildOne");
  }

  private void testUrlParsing(String url, String protocol, String host, int port, String path) throws Exception {
    importer.parseUrl(url);
    assertEquals(protocol, importer.getRemoteProtocol());
    assertEquals(host, importer.getRemoteHostname());
    assertEquals(port, importer.getRemotePort());
    assertEquals(path, PathParser.render(importer.getRemotePath()));
  }

  @Test
  public void testParsingBadUrl() throws Exception {
    try {
      importer.parseUrl("blah");
      fail("should have exception");
    }
    catch (Exception e) {
      assertEquals("blah is not a valid URL.", e.getMessage());
    }
  }

  @Test
  public void testImportingWiki() throws Exception {
    localRoot = InMemoryPage.makeRoot("LocalRoot");
    importer.importWiki(localRoot);

    assertEquals(2, localRoot.getChildren().size());
    assertEquals(3, imports.size());
    assertEquals(0, errors.size());
  }

  @Test
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
    addLocalPageWithImportProperty(localRoot, "OtherImportRoot", true);

    importer.importWiki(localRoot);
  }

  @Test
  public void testWholeTreeOrphaned() throws Exception {
    importer.importWiki(localRoot);

    remoteRoot.getChildPage("PageOne").remove();

    importer.importWiki(localRoot);

    assertFalse(localRoot.hasChildPage("PageOne"));
  }

  @Test
  public void testContextIsNotOrphanWhenUpdatingNonRoot() throws Exception {
    addLocalPageWithImportProperty(localRoot, "PageOne", false);
    importer.parseUrl("http://localhost:" + FitNesseUtil.PORT + "/PageOne");

    importer.importWiki(localRoot.getChildPage("PageOne"));

    assertEquals(0, importer.getOrphans().size());
  }

  @Test
  public void testAutoUpdatePropertySetOnRoot() throws Exception {
    addLocalPageWithImportProperty(localRoot, "PageOne", false);
    importer.parseUrl("http://localhost:" + FitNesseUtil.PORT + "/PageOne");
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

  @Test
  public void testAutoUpdate_NewPage() throws Exception {
    importer.setAutoUpdateSetting(true);
    importer.enterChildPage(pageOne, Clock.currentDate());

    WikiImportProperty importProps = WikiImportProperty.createFrom(pageOne.getData().getProperties());
    assertTrue(importProps.isAutoUpdate());
  }

  @Test
  public void testAutoUpdateWhenRemotePageNotModified() throws Exception {
    importer.enterChildPage(pageOne, Clock.currentDate());
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

    WikiPagePath pagePath = page.getFullPath();
    WikiImportProperty importProps = new WikiImportProperty("http://localhost:" + FitNesseUtil.PORT + "/" + PathParser.render(pagePath));
    if (isRoot)
      importProps.setRoot(true);
    importProps.addTo(data.getProperties());
    page.commit(data);

    return page;
  }

  @Override
  public void pageImported(WikiPage page) {
    imports.add(page);
  }

  @Override
  public void pageImportError(WikiPage page, Exception e) {
    errors.add(e);
  }
}
