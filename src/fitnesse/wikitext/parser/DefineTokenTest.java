package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DefineTokenTest {
    @Test public void scansDefine() {
        ParserTest.assertScansTokenType("!define x {y}", TokenType.Define, true);
    }

    @Test public void translatesDefine() throws Exception {
        assertTranslatesDefine("x", "y", "y");
        assertTranslatesDefine("x", "''y''", "<i>y</i>"  + HtmlElement.endl);
    }

    private void assertTranslatesDefine(String name, String inputValue, String definedValue) throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT");
        PageCrawler crawler = root.getPageCrawler();
        WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));

        ParserTest.assertTranslates(pageOne,
                "!define " + name + " {" + inputValue + "}",
                "<span class=\"meta\">variable defined: " + name + "=" + inputValue + "</span>");
        assertEquals(definedValue, pageOne.getData().getVariable(name));
    }
}
