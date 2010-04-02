package fitnesse.wikitext.parser;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class VariableTest {
    @Test public void scansVariables() {
        ParserTest.assertScansTokenType("${x}", SymbolType.Variable, true);
    }

    @Test public void translatesVariables() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne");
        PageData data = new PageData(pageOne, "!define x {y}");
        pageOne.commit(data);
        ParserTest.assertTranslatesTo(pageOne, "${x}", "y");
    }

    @Test public void translatesVariablesFromParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {y}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTest.assertTranslatesTo(child, "${x}", "y");
    }
}
