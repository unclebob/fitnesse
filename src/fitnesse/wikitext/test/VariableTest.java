package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
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
        ParserTest.assertTranslatesTo(pageOne, "${z}", "<span class=\"meta\">undefined variable: z</span>");
    }

    @Test public void evaluatesVariablesAtCurrentLocation() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n${x}\n!define x {z}\n${x}");
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
                "<br/>y<br/><span class=\"meta\">variable defined: x=z</span>" + HtmlElement.endl +
                "<br/>z");
    }

    @Test public void evaluatesVariablesFromParent() throws Exception {
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
