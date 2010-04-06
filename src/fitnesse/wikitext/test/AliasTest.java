package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class AliasTest {
    @Test
    public void scansAliases() {
        ParserTest.assertScansTokenType("[[tag][link]]", SymbolType.Alias, true);
        ParserTest.assertScansTokenType("[ [tag][link]]", SymbolType.Alias, false);
    }

    @Test
    public void parsesAliases() {
        ParserTest.assertParses("[[tag][PageOne]]", "SymbolList[Alias[SymbolList[Text], WikiWord]]");
        ParserTest.assertParses("[[PageOne][PageOne]]", "SymbolList[Alias[SymbolList[WikiWord], WikiWord]]");
    }

    @Test
    public void translatesAliases() throws Exception {
        WikiPage page = new TestRoot().makePage("PageOne");
        ParserTest.assertTranslatesTo(page, "[[tag][link]]", link("tag", "link"));
        ParserTest.assertTranslatesTo(page, "[[tag][PageOne]]", link("tag", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[''tag''][PageOne]]", link("<i>tag</i>" + HtmlElement.endl, "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[you're it][PageOne]]", link("you're it", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[PageOne][IgnoredPage]]", link("PageOne", "PageOne"));
    }

    private String link(String body, String href) {
        return "<a href=\"" + href + "\">" + body + "</a>";
    }

}
