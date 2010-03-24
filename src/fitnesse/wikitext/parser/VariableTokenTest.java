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
        WikiPage pageOne = new TestRoot().makePage("PageOne");
        pageOne.getData().addVariable("x", "y");
        ParserTest.assertTranslates(pageOne, "${x}", "y");
    }

    @Test public void translatesVariablesFromParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne");
        parent.getData().addVariable("x", "y");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTest.assertTranslates(child, "${x}", "y");
    }
}
