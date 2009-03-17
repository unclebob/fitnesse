// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.text.SimpleDateFormat;
import java.util.Date;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtensionTest;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;

public class WikiImportPropertyTest extends RegexTestCase {
  private WikiImportProperty property;
  private WikiPage page;

  public void setUp() {
    property = new WikiImportProperty("");
  }

  public void testSource() throws Exception {
    property = new WikiImportProperty("import source");
    assertEquals("import source", property.getSourceUrl());
    assertEquals("import source", property.get("Source"));
  }

  public void testIsRoot() throws Exception {
    assertFalse(property.isRoot());
    assertFalse(property.has("IsRoot"));

    property.setRoot(true);

    assertTrue(property.isRoot());
    assertTrue(property.has("IsRoot"));
  }

  public void testAutoUpdate() throws Exception {
    assertFalse(property.isAutoUpdate());
    assertFalse(property.has("AutoUpdate"));

    property.setAutoUpdate(true);

    assertTrue(property.isAutoUpdate());
    assertTrue(property.has("AutoUpdate"));
  }

  public void testLastUpdated() throws Exception {
    SimpleDateFormat format = WikiPageProperty.getTimeFormat();
    Date date = new Date();
    property.setLastRemoteModificationTime(date);

    assertEquals(format.format(date), format.format(property.getLastRemoteModificationTime()));

    assertEquals(format.format(date), property.get("LastRemoteModification"));
  }

  public void testFailedCreateFromProperty() throws Exception {
    assertNull(WikiImportProperty.createFrom(new WikiPageProperty()));
  }

  public void testCreateFromProperty() throws Exception {
    WikiPageProperty rawImportProperty = property.set(WikiImportProperty.PROPERTY_NAME);
    rawImportProperty.set("IsRoot");
    rawImportProperty.set("AutoUpdate");
    rawImportProperty.set("Source", "some source");
    Date date = new Date();
    rawImportProperty.set("LastRemoteModification", WikiPageProperty.getTimeFormat().format(date));

    WikiImportProperty importProperty = WikiImportProperty.createFrom(property);
    assertEquals("some source", importProperty.getSourceUrl());
    assertTrue(importProperty.isRoot());
    assertTrue(importProperty.isAutoUpdate());
    SimpleDateFormat format = WikiPageProperty.getTimeFormat();
    assertEquals(format.format(date), format.format(importProperty.getLastRemoteModificationTime()));
  }

  public void testAddtoProperty() throws Exception {
    WikiImportProperty importProperty = new WikiImportProperty("some source");
    importProperty.setRoot(true);
    importProperty.setAutoUpdate(true);
    importProperty.addTo(property);

    WikiImportProperty importProperty2 = WikiImportProperty.createFrom(property);
    assertEquals("some source", importProperty2.getSourceUrl());
    assertTrue(importProperty2.isRoot());
    assertTrue(importProperty2.isAutoUpdate());
  }

  // Tests for the rendering of import specific page details
  private WikiPage root;
  private PageCrawler crawler;

  public void pageRenderingSetUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
  }

  private SimpleResponse requestPage(String name) throws Exception {
    MockRequest request = new MockRequest();
    request.setResource(name);
    Responder responder = new WikiPageResponder();
    return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
  }

  public void testVirtualPageIndication() throws Exception {
    pageRenderingSetUp();

    WikiPage targetPage = crawler.addPage(root, PathParser.parse("TargetPage"));
    crawler.addPage(targetPage, PathParser.parse("ChildPage"));
    WikiPage linkPage = (BaseWikiPage) crawler.addPage(root, PathParser.parse("LinkPage"));
    VirtualCouplingExtensionTest.setVirtualWiki(linkPage, "http://localhost:" + FitNesseUtil.port + "/TargetPage");

    FitNesseUtil.startFitnesse(root);
    SimpleResponse response = null;
    try {
      response = requestPage("LinkPage.ChildPage");
    }
    finally {
      FitNesseUtil.stopFitnesse();
    }

    assertSubString("<body class=\"virtual\">", response.getContent());
  }

  public void testImportedPageIndication() throws Exception {
    pageRenderingSetUp();

    page = crawler.addPage(root, PathParser.parse("SamplePage"));
    PageData data = page.getData();
    WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);

    String content = getContentAfterSpecialImportHandling();

    assertSubString("<body class=\"imported\">", content);
  }

  public void testEditActions() throws Exception {
    pageRenderingSetUp();

    page = crawler.addPage(root, PathParser.parse("SamplePage"));
    PageData data = page.getData();
    page.commit(data);
    String content = getContentAfterSpecialImportHandling();

    assertNotSubString("Edit Locally", content);
    assertNotSubString("Edit Remotely", content);

    WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);
    content = getContentAfterSpecialImportHandling();

    assertSubString("<a href=\"SamplePage?edit\" accesskey=\"e\">Edit Locally</a>", content);
    assertSubString("<a href=\"blah?responder=edit&amp;redirectToReferer=true&amp;redirectAction=importAndView\" accesskey=\"e\">Edit Remotely</a>", content);
  }

  private String getContentAfterSpecialImportHandling() throws Exception {
    HtmlPage html = new HtmlPageFactory().newPage();
    WikiImportProperty.handleImportProperties(html, page, page.getData());
    return html.html();
  }

}
