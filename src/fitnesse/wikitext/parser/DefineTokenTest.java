package fitnesse.wikitext.parser;

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
        WikiPage root = InMemoryPage.makeRoot("RooT");
        PageCrawler crawler = root.getPageCrawler();
        WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));

        ParserTest.assertTranslates(pageOne, "!define x {y}", "<span class=\"meta\">variable defined: x=y</span>");
        assertEquals("y", pageOne.getData().getVariable("x"));
    }
}
