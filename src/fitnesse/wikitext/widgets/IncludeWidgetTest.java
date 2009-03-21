// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.VirtualCouplingExtensionTest;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.WidgetBuilder;

public class IncludeWidgetTest extends WidgetTestCase {

  protected WikiPage root;
  protected WikiPage page1;
  protected WikiPage page2;
  protected WikiPage child1;
  protected WikiPage child2;
  protected WikiPage grandChild1;
  protected PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
    page2 = crawler.addPage(root, PathParser.parse("PageTwo"), "page '''two'''");
    child1 = crawler.addPage(page2, PathParser.parse("ChildOne"), "child page");
    child2 = crawler.addPage(page2, PathParser.parse("ChildTwo"), "child two");
    grandChild1 = crawler.addPage(child1, PathParser.parse("GrandChildOne"), "grand child one");
  }

  public void tearDown() throws Exception {
  }

  private IncludeWidget createIncludeWidget(WikiPage wikiPage, String includedPageName) throws Exception {
    return createIncludeWidget(new WidgetRoot(wikiPage), includedPageName);
  }

  private IncludeWidget createIncludeWidget(ParentWidget widgetRoot, String includedPageName) throws Exception {
    return new IncludeWidget(widgetRoot, "!include " + includedPageName);
  }

  protected String getRegexp() {
    return IncludeWidget.REGEXP;
  }

  public void testIsCollapsable() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "PageOne");
    final String result = widget.render();
    assertSubString("class=\"collapsable\"", result);
  }

  public void testSeamlessIsNotCollapsable() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "-seamless PageOne");
    final String result = widget.render();
    assertNotSubString("class=\"collapsable\"", result);
  }

  public void testCollapsedIsHidden() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "-c PageOne");
    final String result = widget.render();
    assertNotSubString("class=\"collapsable\"", result);
    assertSubString("class=\"hidden\"", result);
    assertSubString("collapsableClosed.gif", result);
  }

  public void testHasEditLink() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "PageOne");
    final String result = widget.render();
    assertHasRegexp("^.*href.*edit.*$", result);
  }

  public void testRegexp() throws Exception {
    assertMatchEquals("!include SomePage", "!include SomePage");
    assertMatchEquals("!include SomePage\n", "!include SomePage\n");
    assertMatchEquals("abc\n" + "!include SomePage\nxyz", "!include SomePage\n");
    assertMatchEquals("!include .SomePage.ChildPage", "!include .SomePage.ChildPage");
    assertNoMatch("!include nonWikiWord");
    assertNoMatch(" " + "!include WikiWord");
  }

  public void testRegexpWithOptions() throws Exception {
    assertMatchEquals("!include -c SomePage", "!include -c SomePage");
    assertMatchEquals("!include -setup SomePage", "!include -setup SomePage");
    assertMatchEquals("!include  -setup SomePage", "!include  -setup SomePage");
    assertMatchEquals("!include -teardown SomePage", "!include -teardown SomePage");
    assertMatchEquals("!include  -teardown SomePage", "!include  -teardown SomePage");
    assertMatchEquals("!include -seamless SomePage", "!include -seamless SomePage");
    assertMatchEquals("!include  -seamless SomePage", "!include  -seamless SomePage");
  }

  public void testSetUpParts() throws Exception {
    IncludeWidget widget = new IncludeWidget(new WidgetRoot(root), "!include -setup SomePage");
    assertSubString("Set Up: ", widget.render());
    assertSubString("class=\"setup\"", widget.render());
    assertSubString("class=\"hidden\"", widget.render());
  }

  public void testSetUpCollapsed() throws Exception {
    ParentWidget widgetRoot = new WidgetRoot(root);
    widgetRoot.addVariable(IncludeWidget.COLLAPSE_SETUP, "true");
    IncludeWidget widget = new IncludeWidget(widgetRoot, "!include -setup SomePage");
    assertSubString("Set Up: ", widget.render());
    assertSubString("class=\"setup\"", widget.render());
    assertSubString("class=\"hidden\"", widget.render());
  }

  public void testSetUpUncollapsed() throws Exception {
    ParentWidget widgetRoot = new WidgetRoot(root);
    widgetRoot.addVariable(IncludeWidget.COLLAPSE_SETUP, "false");
    IncludeWidget widget = new IncludeWidget(widgetRoot, "!include -setup SomePage");
    assertSubString("Set Up: ", widget.render());
    assertSubString("class=\"setup\"", widget.render());
    assertSubString("class=\"collapsable\"", widget.render());
  }

  public void testTearDownParts() throws Exception {
    IncludeWidget widget = new IncludeWidget(new WidgetRoot(root), "!include -teardown SomePage");
    assertSubString("Tear Down: ", widget.render());
    assertSubString("class=\"teardown\"", widget.render());
    assertSubString("class=\"hidden\"", widget.render());
  }

  public void testTearDownCollapsed() throws Exception {
    ParentWidget widgetRoot = new WidgetRoot(root);
    widgetRoot.addVariable(IncludeWidget.COLLAPSE_TEARDOWN, "true");
    IncludeWidget widget = new IncludeWidget(widgetRoot, "!include -teardown SomePage");
    assertSubString("Tear Down: ", widget.render());
    assertSubString("class=\"teardown\"", widget.render());
    assertSubString("class=\"hidden\"", widget.render());
  }

  public void testTearDownUncollapsed() throws Exception {
    ParentWidget widgetRoot = new WidgetRoot(root);
    widgetRoot.addVariable(IncludeWidget.COLLAPSE_TEARDOWN, "false");
    IncludeWidget widget = new IncludeWidget(widgetRoot, "!include -teardown SomePage");
    assertSubString("Tear Down: ", widget.render());
    assertSubString("class=\"teardown\"", widget.render());
    assertSubString("class=\"collapsable\"", widget.render());
  }

  public void testLiteralsGetRendered() throws Exception {
    verifyLiteralsGetRendered("", "LiteralPage");
  }

  public void testLiteralsGetRenderedSeamless() throws Exception {
    verifyLiteralsGetRendered("-seamless ", "LiteralPage");
  }

  private void verifyLiteralsGetRendered(String option, String pageName)
  throws Exception {
    crawler.addPage(root, PathParser.parse(pageName), "!-one-!, !-two-!, !-three-!");
    ParentWidget widgetRoot = new WidgetRoot(page1);
    IncludeWidget widget = createIncludeWidget(widgetRoot, option + pageName);
    final String result = widget.render();
    assertSubString("one, two, three", result);
    assertEquals("one", widgetRoot.getLiteral(0));
    assertEquals("two", widgetRoot.getLiteral(1));
    assertEquals("three", widgetRoot.getLiteral(2));
  }

  public void testPageNameOnSetUpPage() throws Exception {
    verifyPageNameResolving("-setup ", "IncludingPage");
  }

  public void testPageNameOnTearDownPage() throws Exception {
    verifyPageNameResolving("-teardown ", "IncludingPage");
  }

  public void testPageNameOnRegularPage() throws Exception {
    verifyPageNameResolving("", "IncludedPage");
  }

  private void verifyPageNameResolving(String option, String expectedPageName) throws Exception {
    crawler.addPage(root, PathParser.parse("IncludedPage"), "This is IncludedPage\nincluded page name is ${PAGE_NAME}\n");
    crawler.addPage(root, PathParser.parse("IncludingPage"));
    ParentWidget widgetRoot = new WidgetRoot("This is IncludingPage\n" + "!include " + option + "IncludedPage",
        root.getChildPage("IncludingPage"), WidgetBuilder.htmlWidgetBuilder);
    String content = widgetRoot.render();
    assertHasRegexp("included page name is <a href=\"" + expectedPageName + "\">" + expectedPageName , content);
  }


  public void testRenderWhenMissing() throws Exception {
    verifyRenderWhenMissing("MissingPage");
  }

  public void testRenderWhenMissingSeamless() throws Exception {
    verifyRenderWhenMissing("-seamless MissingPage");
  }

  private void verifyRenderWhenMissing(String optionAndPageName)
  throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, optionAndPageName);
    assertHasRegexp("MissingPage.*does not exist", widget.render());
  }

  public void testNoNullPointerWhenIncludingFromRootPage() throws Exception {
    verifyNoNullPointerWhenIncludingFromRootPage(".PageOne");
  }

  public void testNoNullPointerWhenIncludingFromRootPageSeamless() throws Exception {
    verifyNoNullPointerWhenIncludingFromRootPage("-seamless .PageOne");
  }

  private void verifyNoNullPointerWhenIncludingFromRootPage(String optionAndPageName)
  throws Exception {
    IncludeWidget widget = createIncludeWidget(root, optionAndPageName);
    assertHasRegexp("page one", widget.render());
  }

  public void testIncludingVariables() throws Exception {
    verifyIncludingVariables("");
  }

  public void testIncludingVariablesSeamless() throws Exception {
    verifyIncludingVariables("-seamless ");
  }

  private void verifyIncludingVariables(String option)
  throws Exception {
    crawler.addPage(root, PathParser.parse("VariablePage"), "This is VariablePage\n!define X {blah!}\n");
    crawler.addPage(root, PathParser.parse("IncludingPage"));
    ParentWidget widgetRoot = new WidgetRoot("This is IncludingPage\n" + "!include " + option + ".VariablePage\nX=${X}",
        root.getChildPage("IncludingPage"), WidgetBuilder.htmlWidgetBuilder);
    String content = widgetRoot.render();
    assertHasRegexp("X=blah!", content);
  }

  public void testVirtualIncludeNotFound() throws Exception {
    verifyVirtualIncludeNotFound("IncludedPage");
  }

  public void testVirtualIncludeNotFoundSeamless() throws Exception {
    verifyVirtualIncludeNotFound("-seamless IncludedPage");
  }

  private void verifyVirtualIncludeNotFound(String optionAndPageName)
  throws Exception {
    ProxyPage virtualPage = new ProxyPage("VirtualPage", root, "localhost", 9999, PathParser.parse("RealPage.VirtualPage"));
    IncludeWidget widget = createIncludeWidget(virtualPage, optionAndPageName);
    String output = widget.render();
    assertHasRegexp("IncludedPage.* does not exist", output);
  }

  public void testVirtualInclude() throws Exception {
    String virtualWikiURL = "http://localhost:" + FitNesseUtil.port + "/PageTwo";
    VirtualCouplingExtensionTest.setVirtualWiki(page1, virtualWikiURL);
    FitNesseUtil.startFitnesse(root);
    try {
      IncludeWidget widget = createIncludeWidget(page1, ".PageOne.ChildOne");
      String result = widget.render();
      verifySubstrings(new String[]{"child page", ".PageOne.ChildOne"}, result);
    }
    finally {
      FitNesseUtil.stopFitnesse();
    }
  }

  public void testDeepVirtualInclude() throws Exception {
    WikiPagePath atPath = PathParser.parse("AcceptanceTestPage");
    WikiPagePath includedPagePath = PathParser.parse("AcceptanceTestPage.IncludedPage");
    WikiPagePath includingPagePath = PathParser.parse("AcceptanceTestPage.IncludingPage");
    WikiPagePath childOfIncludingPagePath = PathParser.parse("AcceptanceTestPage.IncludingPage.ChildIncludingPage");
    crawler.addPage(root, atPath);
    crawler.addPage(root, includedPagePath, "included page");
    crawler.addPage(root, includingPagePath, "!include .AcceptanceTestPage.IncludedPage");
    crawler.addPage(root, childOfIncludingPagePath, "!include .AcceptanceTestPage.IncludedPage");

    String virtualWikiURL = "http://localhost:" + FitNesseUtil.port + "/AcceptanceTestPage";
    WikiPage alternateRoot = InMemoryPage.makeRoot("RooT");
    WikiPagePath virtualPagePath = PathParser.parse("VirtualPage");
    WikiPage virtualHost = crawler.addPage(alternateRoot, virtualPagePath, "virtual host\n!contents\n");
    VirtualCouplingExtensionTest.setVirtualWiki(virtualHost, virtualWikiURL);

    FitNesseUtil.startFitnesse(root);
    try {
      WikiPage virtualChild = crawler.getPage(alternateRoot, PathParser.parse("VirtualPage.IncludingPage"));
      PageData data = virtualChild.getData();
      String result = data.getHtml();
      verifySubstrings(new String[]{"included page", "AcceptanceTestPage.IncludedPage"}, result);
    }
    finally {
      FitNesseUtil.stopFitnesse();
    }
  }

  public void testRenderIncludedSibling() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "PageOne");
    final String result = widget.render();
    verifyRegexes(new String[]{"page one", "Included page: .*PageOne"}, result);
  }

  public void testRenderIncludedSiblingSeamless() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, "-seamless PageOne");
    final String result = widget.render();
    verifySubstrings(new String[]{"page one<br/>"}, result);
  }

  public void testRenderIncludedNephew() throws Exception {
    IncludeWidget widget = createIncludeWidget(page1, ".PageTwo.ChildOne");
    String result = widget.render();
    verifyRegexes(new String[]{"child page", "class=\"included\""}, result);
  }

  public void testRenderSubPage() throws Exception {
    IncludeWidget widget = createIncludeWidget(page2, ">ChildOne");
    String result = widget.render();
    verifyRegexes(new String[]{"child page", "class=\"included\""}, result);
  }

  public void testRenderBackwardsSearch() throws Exception {
    IncludeWidget widget = createIncludeWidget(grandChild1, "<PageTwo.ChildTwo");
    String result = widget.render();
    verifyRegexes(new String[]{"child two", "class=\"included\""}, result);
  }

  private void verifySubstrings(String[] subStrings, String result) {
    for (int i = 0; i < subStrings.length; i++) {
      assertSubString(subStrings[i], result);
    }
  }

  private void verifyRegexes(String[] regexes, String result) {
    for (int i = 0; i < regexes.length; i++) {
      assertHasRegexp(regexes[i], result);
    }
  }

}
