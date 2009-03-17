// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class TOCWidgetTest extends WidgetTestCase {
  private WikiPage root;
  private WikiPage parent, parent2, child1P2, child2P2;
  private PageCrawler crawler;

  //===================================================[ SetUp / TearDown
  //
  @Override
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    parent = crawler.addPage(root, PathParser.parse("ParenT"), "parent");
    crawler.addPage(root, PathParser.parse("ParentTwo"), "parent two");

    crawler.addPage(parent, PathParser.parse("ChildOne"), "content");
    crawler.addPage(parent, PathParser.parse("ChildTwo"), "content");
    //Regracing
    parent2 = crawler.addPage(root, PathParser.parse("ParenT2"), "parent2");
    child1P2 = crawler.addPage(parent2, PathParser.parse("Child1Page"), "content");
    child2P2 = crawler.addPage(parent2, PathParser.parse("Child2Page"), "content");
  }

  @Override
  public void tearDown() throws Exception {
  }

  //===================================================[ Miscellaneous
  //
  @Override
  protected String getRegexp() {
    return TOCWidget.REGEXP;
  }

  //===================================================[ Matchers
  //
  public void testMatch() throws Exception {
    assertMatchEquals("!contents\n", "!contents");
    assertMatchEquals("!contents -R\n", "!contents -R");
    assertMatchEquals("!contents\r", "!contents");
    assertMatchEquals("!contents -R\r", "!contents -R");
    assertMatchEquals(" !contents\n", null);
    assertMatchEquals(" !contents -R\n", null);
    assertMatchEquals("!contents zap\n", null);
    assertMatchEquals("!contents \n", "!contents ");
    // -R[0-9]...
    assertMatchEquals("!contents -R0\n", "!contents -R0");
    assertMatchEquals("!contents -R1\n", "!contents -R1");
    assertMatchEquals("!contents -R99\n", "!contents -R99");
    assertMatchEquals("!contents -Rx\n", null);

    // Regracing
    assertMatchEquals("!contents -g\n", "!contents -g");
    assertMatchEquals("!contents -R -g\n", "!contents -R -g");
    assertMatchEquals("!contents -g\r", "!contents -g");
    assertMatchEquals("!contents -R -g\r", "!contents -R -g");
    assertMatchEquals(" !contents    -g\n", null);
    assertMatchEquals(" !contents -R -g\n", null);
    assertMatchEquals("!contents -gx\n", null);
    assertMatchEquals("!contents -g \n", "!contents -g ");

    // Property suffix
    assertMatchEquals("!contents -p\n", "!contents -p");
    assertMatchEquals("!contents -R -p\n", "!contents -R -p");
    assertMatchEquals("!contents -p\r", "!contents -p");
    assertMatchEquals("!contents -R -p\r", "!contents -R -p");
    assertMatchEquals("!contents -p \n", "!contents -p ");
    assertMatchEquals("!contents -g -p\n", "!contents -g -p");
    assertMatchEquals("!contents  -R2  -g  -p  \n", "!contents  -R2  -g  -p  ");
    assertMatchEquals("!contents -p -g\n", "!contents -p -g");
    assertMatchEquals("!contents -R -p -g\n", "!contents -R -p -g");
    assertMatchEquals(" !contents    -p\n", null);
    assertMatchEquals(" !contents -R -p\n", null);
    assertMatchEquals("!contents -px\n", null);

    // Filter suffix
    assertMatchEquals("!contents -f\n", "!contents -f");
    assertMatchEquals("!contents -R -f\n", "!contents -R -f");
    assertMatchEquals("!contents -f \n", "!contents -f ");
    assertMatchEquals("!contents -g -p -f\n", "!contents -g -p -f");
    assertMatchEquals("!contents -f -p -g\n", "!contents -f -p -g");
    assertMatchEquals("!contents -R -p -g -f\n", "!contents -R -p -g -f");
    assertMatchEquals("!contents -fx\n", null);

    // Help suffix
    assertMatchEquals("!contents -h\n", "!contents -h");
    assertMatchEquals("!contents -R -h\n", "!contents -R -h");
    assertMatchEquals("!contents -h \n", "!contents -h ");
    assertMatchEquals("!contents -g -p -h\n", "!contents -g -p -h");
    assertMatchEquals("!contents -h -p -g\n", "!contents -h -p -g");
    assertMatchEquals("!contents -R -p -g -f -h\n", "!contents -R -p -g -f -h");
    assertMatchEquals("!contents -hx\n", null);
  }

  //===================================================[ Structural Testing
  // The tests in this section deal solely with top-level and multi-level
  // structures produced by !contents.
  // DeanW: ... and they are an annoying pain in the ass, because they break everytime
  // you tweak the look and feel!

  public void testTocOnRoot() throws Exception {
    TOCWidget widget = new TOCWidget(new WidgetRoot(root), "!contents\n");
    String html = widget.render();
    assertHasRegexp("ParenT", html);
    assertHasRegexp("ParentTwo", html);
  }

  public void testNoGrandchildren() throws Exception {
    assertHtmlWithNoHierarchy(renderNormalTOCWidget());
    assertHtmlWithNoHierarchy(renderHierarchicalTOCWidget());
  }

  public void testWithGrandchildren() throws Exception {
    addGrandChild(parent, "ChildOne");
    assertHtmlWithNoHierarchy(renderNormalTOCWidget());
    assertHtmlWithGrandChild(renderHierarchicalTOCWidget());
  }

  public void testWithGreatGrandchildren() throws Exception {
    addGrandChild(parent, "ChildOne");
    addGreatGrandChild(parent, "ChildOne");
    assertHtmlWithNoHierarchy(renderNormalTOCWidget());
    assertHtmlWithGreatGrandChild(renderHierarchicalTOCWidget());
  }

  public void testIsNotHierarchical() throws Exception {
    assertFalse(new TOCWidget(new WidgetRoot(parent), "!contents\n").isRecursive());
  }

  public void testIsHierarchical() throws Exception {
    assertTrue(new TOCWidget(new WidgetRoot(parent), "!contents -R\n").isRecursive());
  }

  private WikiPage addGrandChild(WikiPage parent, String childName)
    throws Exception {
    crawler.addPage(parent.getChildPage(childName), PathParser.parse("GrandChild"), "content");
    return parent.getChildPage(childName).getChildPage("GrandChild");
  }

  private WikiPage addGreatGrandChild(WikiPage parent, String childName)
    throws Exception {
    crawler.addPage(parent.getChildPage(childName).getChildPage("GrandChild"), PathParser.parse("GreatGrandChild"), "content");
    return parent.getChildPage(childName).getChildPage("GrandChild").getChildPage("GreatGrandChild");
  }

  //--------------------------------------[ Renderers for Hierarchy
  //
  private String renderNormalTOCWidget()
    throws Exception {
    return new TOCWidget(new WidgetRoot(parent), "!contents\n").render();
  }

  private void assertHtmlWithNoHierarchy(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"ParenT.ChildOne\">ChildOne</a>", html);
    assertContains("<a href=\"ParenT.ChildTwo\">ChildTwo</a>", html);
  }

  private void assertContains(String expectedSubstring, String actual) {
    assertTrue("Expected substring \"" + expectedSubstring + "\" not found in \"" + actual + "\".", actual.contains(expectedSubstring));
  }

  private void assertDoesNotContain(String expectedSubstring, String actual) {
    assertFalse("Unexpected substring \"" + expectedSubstring + "\" was found in \"" + actual + "\".", actual.contains(expectedSubstring));
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalTOCWidget()
    throws Exception {
    return new TOCWidget(new WidgetRoot(parent), "!contents -R\n").render();
  }

  private void assertHtmlWithGrandChild(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertDoesNotContain("<div class=\"toc3\">", html);
    assertContains("<a href=\"ParenT.ChildOne\">ChildOne</a>", html);
    assertContains("<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>", html);
    assertContains("<a href=\"ParenT.ChildTwo\">ChildTwo</a>", html);
  }

  private void assertHtmlWithGreatGrandChild(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT.ChildOne\">ChildOne</a>", html);
    assertContains("<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>", html);
    assertContains("<a href=\"ParenT.ChildOne.GrandChild.GreatGrandChild\">GreatGrandChild</a>", html);
    assertContains("<a href=\"ParenT.ChildTwo\">ChildTwo</a>", html);
  }

  //===================================================[ Virtual Children
  //
  public void testDisplaysVirtualChildren() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("VirtualParent"));
    PageData data = page.getData();
    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:" + FitNesseUtil.port + "/ParenT");
    page.commit(data);
    try {
      FitNesseUtil.startFitnesse(root);
      TOCWidget widget = new TOCWidget(new WidgetRoot(page), "!contents\n");
      String html = widget.render();
      assertVirtualChildrenHtml(html);
    }
    finally {
      FitNesseUtil.stopFitnesse();
    }
  }

  //--------------------------------------[ Renderers for Virtual Children
  //
  //--  --  --  --  --  --  --  --  --  --
  private void assertVirtualChildrenHtml(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"VirtualParent.ChildOne\">", html);
    assertContains("<i>ChildOne</i>", html);
    assertContains("<a href=\"VirtualParent.ChildTwo\">", html);
    assertContains("<i>ChildTwo</i>", html);
  }

  //===================================================[ Graceful Naming
  //
  public void testWithGreatGrandchildrenRegraced() throws Exception {
    addGrandChild(parent2, "Child1Page");
    addGreatGrandChild(parent2, "Child1Page");
    assertHtmlWithNoHierarchyRegraced(renderNormalRegracedTOCWidget());
    assertHtmlWithGreatGrandChildRegraced(renderHierarchicalRegracedTOCWidgetByVar());
    assertHtmlWithGreatGrandChildRegraced(renderHierarchicalRegracedTOCWidgetByOption());
  }

  //--------------------------------------[ Renderers for Regracing
  //
  private String renderNormalRegracedTOCWidget()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    return new TOCWidget(root, "!contents\n").render();
  }

  private void assertHtmlWithNoHierarchyRegraced(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page</a>", html);
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalRegracedTOCWidgetByVar()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    return new TOCWidget(root, "!contents -R\n").render();
  }

  private String renderHierarchicalRegracedTOCWidgetByOption()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    return new TOCWidget(root, "!contents -R -g\n").render();
  }

  private void assertHtmlWithGreatGrandChildRegraced(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page</a>", html);
  }

  //===================================================[ Properties with Graceful Naming
  //
  public void testWithGreatGrandchildrenRegracedProp() throws Exception {
    setProperties(child1P2, new String[]{"Suite", "Prune"});
    setProperties(child2P2, new String[]{"Suite", "Test", "WikiImport"});
    setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Test"});
    setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suite", "Test"});

    assertHtmlWithNoHierarchyRegracedProp(renderNormalRegracedPropTOCWidget());
    assertHtmlWithGreatGrandChildRegracedProp(renderHierarchicalRegracedPropTOCWidgetByVar());
    assertHtmlWithGreatGrandChildRegracedProp(renderHierarchicalRegracedPropTOCWidgetByOption());

    parent2.getData().addVariable(TOCWidget.PROPERTY_CHARACTERS, "#!%");
    assertHtmlWithGreatGrandChildRegracedPropAlt(renderHierarchicalRegracedPropAltTOCWidget());
  }

  private void setProperties(WikiPage page, String[] propList) throws Exception {
    PageData data = page.getData();
    WikiPageProperties props = data.getProperties();
    for (int i = 0; i < propList.length; i++) {
      String[] parts = propList[i].split("=");
      if (parts.length == 1) props.set(parts[0]);
      else props.set(parts[0], parts[1]);
    }

    page.commit(data);
  }

  //--------------------------------------[ Renderers for Properties with Graceful Names
  //
  private String renderNormalRegracedPropTOCWidget()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    root.addVariable(TOCWidget.PROPERTY_TOC, "true");
    return new TOCWidget(root, "!contents\n").render();
  }

  private void assertHtmlWithNoHierarchyRegracedProp(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page *-</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page *+@</a>", html);
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalRegracedPropTOCWidgetByVar()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    root.addVariable(TOCWidget.PROPERTY_TOC, "true");
    return new TOCWidget(root, "!contents -R\n").render();
  }

  private String renderHierarchicalRegracedPropTOCWidgetByOption()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    return new TOCWidget(root, "!contents -R -p\n").render();
  }

  private void assertHtmlWithGreatGrandChildRegracedProp(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page *-</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child +</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child *+</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page *+@</a>", html);
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalRegracedPropAltTOCWidget()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.REGRACE_TOC, "true");
    root.addVariable(TOCWidget.PROPERTY_CHARACTERS, "#!%");
    return new TOCWidget(root, "!contents -R -p\n").render();
  }

  private void assertHtmlWithGreatGrandChildRegracedPropAlt(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page #-</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child !</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child #!</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page #!%</a>", html);
  }

  //===================================================[ Filter Suffix
  //
  public void testWithGreatGrandchildrenAndFilters() throws Exception {
    setProperties(child1P2, new String[]{"Suites=F1"});
    setProperties(child2P2, new String[]{"Suites=F1,F2"});
    setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2"});
    setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2,F3"});

    assertHtmlWithNoHierarchyFilters(renderNormalFiltersTOCWidget());
    assertHtmlWithGreatGrandChildFilters(renderHierarchicalFiltersTOCWidgetByVar());
    assertHtmlWithGreatGrandChildFilters(renderHierarchicalFiltersTOCWidgetByOption());
  }

  //--------------------------------------[ Renderers for Properties with Graceful Names
  //
  private String renderNormalFiltersTOCWidget()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.FILTER_TOC, "true");
    return new TOCWidget(root, "!contents -g\n").render();
  }

  private void assertHtmlWithNoHierarchyFilters(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a>", html);
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalFiltersTOCWidgetByVar()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.FILTER_TOC, "true");
    return new TOCWidget(root, "!contents -R -g\n").render();
  }

  private String renderHierarchicalFiltersTOCWidgetByOption()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    return new TOCWidget(root, "!contents -R -g -f\n").render();
  }

  private void assertHtmlWithGreatGrandChildFilters(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child (F2)</a>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child (F2,F3)</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a>", html);
  }

  //===================================================[ Help Suffix
  //
  public void testWithGreatGrandchildrenAndHelp() throws Exception {
    setProperties(child1P2, new String[]{"Suites=F1", "Help=Root child 1 help"});
    setProperties(child2P2, new String[]{"Suites=F1,F2", "Help=Root child 2 help"});
    setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2", "Help=Grand child help"});
    setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2,F3", "Help=Great grand child help"});

    assertHtmlWithNoHierarchyHelp(renderNormalHelpTOCWidget());
    assertHtmlWithGreatGrandChildHelp(renderHierarchicalHelpTOCWidgetByVar());
    assertHtmlWithGreatGrandChildHelp(renderHierarchicalHelpTOCWidgetByOption());
  }

  //--------------------------------------[ Renderers for Properties with Graceful Names
  //
  private String renderNormalHelpTOCWidget()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    return new TOCWidget(root, "!contents -g\n").render();
  }

  private void assertHtmlWithNoHierarchyHelp(String html) {
    assertContains("<div class=\"toc1\">", html);
    assertDoesNotContain("<div class=\"toc2\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\" title=\"Root child 1 help\">Child 1 Page</a>", html);
    assertContains("<a href=\"ParenT2.Child2Page\" title=\"Root child 2 help\">Child 2 Page</a>", html);
  }

  //--  --  --  --  --  --  --  --  --  --
  private String renderHierarchicalHelpTOCWidgetByVar()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    root.addVariable(TOCWidget.HELP_TOC, "true");
    return new TOCWidget(root, "!contents -R -g -f\n").render();
  }

  private String renderHierarchicalHelpTOCWidgetByOption()
    throws Exception {
    ParentWidget root = new WidgetRoot(parent2);
    return new TOCWidget(root, "!contents -R -g -f -h\n").render();
  }

  private void assertHtmlWithGreatGrandChildHelp(String html) {
    String hsep = TOCWidget.HELP_PREFIX_DEFAULT;
    assertContains("<div class=\"toc1\">", html);
    assertContains("<div class=\"toc2\">", html);
    assertContains("<div class=\"toc3\">", html);
    assertDoesNotContain("<div class=\"toc4\">", html);
    assertContains("<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a><span class=\"pageHelp\">" + hsep + "Root child 1 help</span>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child (F2)</a><span class=\"pageHelp\">" + hsep + "Grand child help</span>", html);
    assertContains("<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child (F2,F3)</a><span class=\"pageHelp\">" + hsep + "Great grand child help</span>", html);
    assertContains("<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a><span class=\"pageHelp\">" + hsep + "Root child 2 help</span>", html);
  }
}
