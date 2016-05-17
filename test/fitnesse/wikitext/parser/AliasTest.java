package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class AliasTest {
  @Test
  public void scansAliases() {
    ParserTestHelper.assertScansTokenType("[[tag][link]]", "Alias", true);
    ParserTestHelper.assertScansTokenType("[ [tag][link]]", "Alias", false);
  }

  @Test
  public void parsesAliases() {
    ParserTestHelper.assertParses("[[tag][PageOne]]", "SymbolList[Alias[SymbolList[Text], SymbolList[Text]]]");
    ParserTestHelper.assertParses("[[PageOne][PageOne]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]");
    ParserTestHelper.assertParses("[[PageOne][PageOne?edit]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]");
  }

  @Test
  public void translatesAliases() {
    TestSourcePage page = new TestSourcePage().withTarget("PageOne");
    ParserTestHelper.assertTranslatesTo(page, "[[tag][#anchor]]", link("tag", "#anchor"));
    ParserTestHelper.assertTranslatesTo(page, "[[tag][PageOne]]", link("tag", "PageOne"));
    ParserTestHelper.assertTranslatesTo(page, "[[''tag''][PageOne]]", link("<i>tag</i>", "PageOne"));
    ParserTestHelper.assertTranslatesTo(page, "[[you're it][PageOne]]", link("you're it", "PageOne"));
    ParserTestHelper.assertTranslatesTo(page, "[[PageOne][IgnoredPage]]", link("PageOne", "PageOne"));
    ParserTestHelper.assertTranslatesTo(page, "[[tag][PageOne?edit]]", link("tag", "PageOne?edit"));
    ParserTestHelper.assertTranslatesTo(page, "[[tag][http://files/myfile]]", link("tag", "files/myfile"));
    ParserTestHelper.assertTranslatesTo(page, "[[tag][http://example.com/myfile]]", link("tag", "http://example.com/myfile"));
    ParserTestHelper.assertTranslatesTo(page, "[[tag][https://example.com/myfile]]", link("tag", "https://example.com/myfile"));
  }

  @Test
  public void translateNonHttpLinks() {
    TestSourcePage page = new TestSourcePage().withTarget("PageOne");
    ParserTestHelper.assertTranslatesTo(page, "[[tag][notes://example.com/myfile]]", link("tag", "notes://example.com/myfile"));
  }

  @Test
  public void translatesLinksWithSpaces() {
    TestSourcePage page = new TestSourcePage().withTarget("PageOne");
    ParserTestHelper.assertTranslatesTo(page, "[[tag][http://files/my file]]", link("tag", "files/my file"));
  }

  @Test
  public void translatesLinkToNonExistent() {
    ParserTestHelper.assertTranslatesTo(new TestSourcePage().withUrl("NonExistentPage"), "[[tag][NonExistentPage]]",
            "tag<a title=\"create page\" href=\"NonExistentPage?edit&amp;nonExistent=true\">[?]</a>");
  }

  @Test
  public void evaluatesVariablesInLink() throws Exception {
    TestRoot root = new TestRoot();
    WikiPage page = root.makePage("PageOne", "[[tag][PageTwo${x}]]");
    root.makePage("PageTwo3", "hi");
    ParserTestHelper.assertTranslatesTo(page, new TestVariableSource("x", "3"), link("tag", "PageTwo3"));
  }

  @Test
  public void evaluatesLowercaseLink() {
    TestRoot root = new TestRoot();
    WikiPage parent = root.makePage("parent", "[[tag][other_page]]");
    WikiPage page1 = root.makePage(parent, "page", "[[tag][other_page]]");
    root.makePage(parent, "other_page", "hi");
    ParserTestHelper.assertTranslatesTo(page1, link("tag", "parent.other_page"));
  }

  private String link(String body, String href) {
    return "<a href=\"" + href + "\">" + body + "</a>";
  }

}
