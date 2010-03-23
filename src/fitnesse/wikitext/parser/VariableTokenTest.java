package fitnesse.wikitext.parser;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class VariableTokenTest {
    @Test public void scansVariables() {
        ParserTest.assertScansTokenType("${x}", TokenType.Variable, true);
    }
    
    @Test public void translatesVariables() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT");
        PageCrawler crawler = root.getPageCrawler();
        WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));
        pageOne.getData().addVariable("x", "y");
        ParserTest.assertTranslates(pageOne, "${x}", "y");
    }
}
