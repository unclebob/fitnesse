package fitnesse.wikitext.parser;

import static fitnesse.wikitext.parser.ParserTestHelper.assertParses;
import static fitnesse.wikitext.parser.ParserTestHelper.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import org.junit.Test;

public class IncludeTest {
  @Test
  public void scansIncludes() {
    ParserTestHelper.assertScansTokenType("!include name", "Include", true);
  }

  @Test
  public void parsesIncludes() {
    assertParses("!include PageTwo\n", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("|!include PageTwo|\n", "SymbolList[Table[TableRow[TableCell[Include[Text, WikiWord, Text, Style[Text]]]]]]");
    assertParses("!include PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -c PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include <PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -setup PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -teardown PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -h PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -h .SuitePage.PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include <PageTwo>", "SymbolList[Include[Text, Text, Text, Style[Text]]]");
    assertParses("!style_red{\n!include PageTwo}", "SymbolList[Style[SymbolList[Newline, Include[Text, WikiWord, Text, Style[Text]]]]]");
  }

  @Test
  public void parseIncludesWithVariable() {
    Symbol result = parse("!define x {PageOne}\n!include ${x}");
    assertEquals("PageOne", result.childAt(2).childAt(1).getContent());
    result = parse("!define x {Page}\n!define y {One}\n!include ${x}${y}");
    assertEquals("PageOne", result.childAt(4).childAt(1).getContent());
  }

  @Test
  public void parsesIncludeNonWikiWordPages() {
    assertParses("!include -h SuitePage.nonwikipage.PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -h nonwikipage.SuitePage.PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -h .nonwikipage.SuitePage.PageTwo", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
    assertParses("!include -h SuitePage.PageTwo.nonwikipage", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
  }

  @Test
  public void parsesIncludeSingleNonWikiWordPage() {
    assertParses("!include -h nonwikipage", "SymbolList[Include[Text, WikiWord, Text, Style[Text]]]");
  }

  @Test
  public void translatesIncludedSibling() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
    root.makePage("PageTwo", "page ''two''");

    String result = ParserTestHelper.translateTo(currentPage);

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "Included page: <a href=\"PageTwo\">PageTwo</a> <a href=\"PageTwo?edit&amp;redirectToReferer=true&amp;redirectAction=\" class=\"edit\">(edit)</a>");
    assertContains(result, "page <i>two</i>");
  }

  @Test
  public void translatesIncludeWithChildReference() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
    WikiPage pageTwo = root.makePage("PageTwo", ">PageTwoChild");
    root.makePage(pageTwo, "PageTwoChild", "stuff");
    String result = ParserTestHelper.translateTo(currentPage);
    assertContains(result, "PageTwo.PageTwoChild");
  }

  @Test
  public void translatesRelativeInclude() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include >PageOneChild");
    root.makePage(currentPage, "PageOneChild", "stuff");
    String result = ParserTestHelper.translateTo(currentPage);
    assertContains(result, "stuff");
  }

  @Test
  public void translatesNestedRelativeInclude() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include >PageOneChild");
    WikiPage pageOneChild = root.makePage(currentPage, "PageOneChild", "!include >PageOneGrandChild");
    root.makePage(pageOneChild, "PageOneGrandChild", "stuff");
    String result = ParserTestHelper.translateTo(currentPage);
    assertContains(result, "stuff");
  }

  @Test
  public void translatesWithNonWikiWord() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include PageTwo.non_wiki");
    WikiPage pageTwo = root.makePage("PageTwo");
    root.makePage(pageTwo, "non_wiki", "page ''two''");

    String result = ParserTestHelper.translateTo(currentPage);

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "Included page: <a href=\"PageTwo.non_wiki\">PageTwo.non_wiki</a> <a href=\"PageTwo.non_wiki?edit&amp;redirectToReferer=true&amp;redirectAction=\" class=\"edit\">(edit)</a>");
    assertContains(result, "page <i>two</i>");
  }

  @Test
  public void translatesWithAllNonWikiWord() {
    TestRoot root = new TestRoot();
    WikiPage currentPage = root.makePage("PageOne", "!include page_two.non_wiki");
    WikiPage pageTwo = root.makePage("page_two");
    root.makePage(pageTwo, "non_wiki", "page ''two''");

    String result = ParserTestHelper.translateTo(currentPage);

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "Included page: <a href=\"page_two.non_wiki\">page_two.non_wiki</a> <a href=\"page_two.non_wiki?edit&amp;redirectToReferer=true&amp;redirectAction=\" class=\"edit\">(edit)</a>");
    assertContains(result, "page <i>two</i>");
  }

  @Test
  public void translatesWithVariable() {
    TestRoot root = new TestRoot();

    String content = "!define one {PageOne}\n" +
                     "!include ${one}\n" +
                     "!define two {PageTwo}\n" +
                     "!include ${two}";
    WikiPage currentPage = root.makePage("PageRoot",  content);

    WikiPage pageOne = root.makePage("PageOne");
    root.makePage(pageOne,"variable","page ''one''");

    WikiPage pageTwo = root.makePage("PageTwo");
    root.makePage(pageTwo,"variable","page ''two''");

    String result = ParserTestHelper.translateTo(currentPage);
    assertContains(result,"Included page: <a href=\"PageOne\">PageOne</a> <a href=\"PageOne?edit&amp;redirectToReferer=true&amp;redirectAction=\" class=\"edit\">(edit)</a>");
    assertContains(result,"Included page: <a href=\"PageTwo\">PageTwo</a> <a href=\"PageTwo?edit&amp;redirectToReferer=true&amp;redirectAction=\" class=\"edit\">(edit)</a>");
  }

  @Test
  public void setupsAreHidden() {
    String result = ParserTestHelper.translateTo(makePageThatIncludesSetup());

    assertContains(result, "class=\"collapsible closed\"");
    assertContains(result, "<a href=\"PageTwo.SetUp\">");
  }

  @Test
  public void teardownsAreHiddenAndMarked() {
    String result = ParserTestHelper.translateTo(makePageThatIncludesTeardown());

    assertContains(result, "class=\"collapsible closed teardown\"");
    assertContains(result, "<a href=\"PageTwo.TearDown\">");
  }

  private TestSourcePage makePageThatIncludesSetup() {
    return new TestSourcePage()
      .withContent("!include -setup >SetUp")
      .withTarget("PageTwo.SetUp")
      .withIncludedPage(new TestSourcePage().withContent("setup"));
  }

  private TestSourcePage makePageThatIncludesTeardown() {
    return new TestSourcePage()
      .withContent("!include -teardown >TearDown")
      .withTarget("PageTwo.TearDown")
      .withIncludedPage(new TestSourcePage().withContent("teardown"));
  }

  @Test
  public void shouldIncludePathWithNonWikiWordFollowedByNewLines() {
    String result = ParserTestHelper.translateTo(makePageThatIncludesPageFromNonWikiWordPath("\n" +
            "\n" +
            "\n" +
            "  "));

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "<a href=\"FrontPage.Tests.non_wiki_word\">");
    assertContains(result, "Hello world!");
  }

  @Test
  public void shouldIncludePathWithNonWikiWordFollowedBySpaces() {
    String result = ParserTestHelper.translateTo(makePageThatIncludesPageFromNonWikiWordPath(" Some other text\n"));

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "<a href=\"FrontPage.Tests.non_wiki_word\">");
    assertContains(result, "Hello world!");
  }

  private TestSourcePage makePageThatIncludesPageFromNonWikiWordPath(String trailingContent) {
    return new TestSourcePage()
            .withContent("\n!include .FrontPage.Tests.non_wiki_word" + trailingContent)
            .withTarget("FrontPage.Tests.non_wiki_word")
            .withIncludedPage(new TestSourcePage().withContent("Hello world!"));
  }

  @Test
  public void translatesSetupWithoutCollapse() {
    String result = ParserTestHelper.translateTo(makePageThatIncludesSetup(), new TestVariableSource("COLLAPSE_SETUP", "false"));

    assertContains(result, "class=\"collapsible\"");
    assertContains(result, "<a href=\"PageTwo.SetUp\">");
  }

  @Test
  public void translatesCollapsed() {
    TestRoot root = new TestRoot();
    WikiPage includingPage = root.makePage("PageOne", "!include -c PageTwo");
    root.makePage("PageTwo", "two");

    String result = ParserTestHelper.translateTo(includingPage);

    assertContains(result, "class=\"collapsible closed\"");
  }

  @Test
  public void translatesSeamless() {
    TestRoot root = new TestRoot();
    WikiPage includingPage = root.makePage("PageOne", "!include -seamless PageTwo");
    root.makePage("PageTwo", "two");

    ParserTestHelper.assertTranslatesTo(includingPage, "two");
  }

  @Test
  public void translatesHelp() {
    TestRoot root = new TestRoot();
    WikiPage includingPage = root.makePage("PageOne", "!include -h PageTwo");

    WikiPage pageWithHelp = root.makePage("PageTwo", "two");
    PageData pageData = pageWithHelp.getData();
    pageData.setAttribute(WikiPageProperty.HELP, "help me");
    pageWithHelp.commit(pageData);

    ParserTestHelper.assertTranslatesTo(includingPage, "help me");
  }

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String HTML_ERR = ""
      + "<div class=\"collapsible\"><ul><li><a href='#' class='expandall'>Expand</a></li><li><a href='#' class='collapseall'>Collapse</a></li></ul>" + NEW_LINE //
      + "\t<p class=\"title\">Included page: %s</p>" + NEW_LINE
      + "\t<div><span class=\"error\">%s</span></div>" + NEW_LINE
      + "</div>" + NEW_LINE;
  @Test
  public void doesNotIncludeParent() {
    TestRoot root = new TestRoot();
    WikiPage parent = root.makePage("ParentPage", "stuff");
    WikiPage currentPage = root.makePage(parent, "PageOne", "!include <ParentPage");
    ParserTestHelper.assertTranslatesTo(currentPage, String.format(HTML_ERR,
        "<a href=\"ParentPage\">&lt;ParentPage</a>",
        "Error! Cannot include parent page (&lt;ParentPage)."));
  }

  @Test
  public void doesNotIncludeInvalidPageName() {
    TestRoot root = new TestRoot();
    WikiPage parent = root.makePage("ParentPage", "stuff");
    WikiPage currentPage = root.makePage(parent, "PageOne", "!include +not.a.+wiki.page");
    ParserTestHelper.assertTranslatesTo(currentPage, String.format(HTML_ERR,
        "+not.a.+wiki.page",
        "Page include failed because the page +not.a.+wiki.page does not have a valid wiki page name."));
  }

  @Test
  public void doesNotIncludeNotExistingPageName() {
    TestRoot root = new TestRoot();
    WikiPage parent = root.makePage("ParentPage", "stuff");
    WikiPage currentPage = root.makePage(parent, "PageOne", "!include NotExistingPage");
    ParserTestHelper.assertTranslatesTo(currentPage, String.format(HTML_ERR,
        "NotExistingPage<a title=\"create page\" href=\"ParentPage.NotExistingPage?edit&amp;nonExistent=true\">[?]</a>",
        "Page include failed because the page NotExistingPage does not exist."));
  }

  private void assertContains(String result, String substring) {
    assertTrue(result, result.contains(substring));
  }

}
