package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class VariableTest {
    @Test public void scansVariables() {
        ParserTest.assertScansTokenType("${x}", SymbolType.Variable, true);
    }

    @Test public void translatesVariables() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}");
        ParserTest.assertTranslatesTo(pageOne, "${x}", "y");
    }

    @Test public void translatesVariablesFromParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {y}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTest.assertTranslatesTo(child, "${x}", "y");
    }

    @Test public void evaluatesVariablesFromParentInCurrentContext() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {${y}}\n");
        WikiPage child = root.makePage(parent, "PageTwo", "!define y {stuff}\n${x}");
        assertTrue(ParserTest.translateTo(child).endsWith("stuff"));
    }
}
