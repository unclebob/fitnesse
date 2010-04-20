package fitnesse.wikitext.test;

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
    public void parsesAliases() throws Exception {
        ParserTest.assertParses("[[tag][PageOne]]", "SymbolList[Alias[SymbolList[Text], SymbolList[Text]]]");
        ParserTest.assertParses("[[PageOne][PageOne]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]");
        ParserTest.assertParses("[[PageOne][PageOne?edit]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]");
    }

    @Test
    public void translatesAliases() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne");
        root.makePage(page, "PageTwo", "hi");
        ParserTest.assertTranslatesTo(page, "[[tag][link]]", link("tag", "link"));
        ParserTest.assertTranslatesTo(page, "[[tag][PageOne]]", link("tag", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[tag][>PageTwo]]", link("tag", "PageOne.PageTwo"));
        ParserTest.assertTranslatesTo(page, "[[''tag''][PageOne]]", link("<i>tag</i>", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[you're it][PageOne]]", link("you're it", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[PageOne][IgnoredPage]]", link("PageOne", "PageOne"));
        ParserTest.assertTranslatesTo(page, "[[tag][PageOne?edit]]", link("tag", "PageOne?edit"));
        ParserTest.assertTranslatesTo(page, "[[tag][http://files/myfile]]", link("tag", "/files/myfile"));
        ParserTest.assertTranslatesTo(page, "[[tag][NonExistentPage]]", "tag<a title=\"create page\" href=\"NonExistentPage?edit&nonExistent=true\">[?]</a>");
    }

    @Test
    public void evaluatesVariablesInLink() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "[[tag][PageTwo${x}]]");
        root.makePage("PageTwo3", "hi");
        ParserTest.assertTranslatesTo(page, new TestVariableSource("x", "3"), link("tag", "PageTwo3"));
    }

    private String link(String body, String href) {
        return "<a href=\"" + href + "\">" + body + "</a>";
    }

}
