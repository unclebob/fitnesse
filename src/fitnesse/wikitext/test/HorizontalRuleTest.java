package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class HorizontalRuleTest {
    @Test public void scansHorizontalRules() {
        ParserTest.assertScansTokenType("----", "HorizontalRule", true);
        ParserTest.assertScansTokenType("------", "HorizontalRule", true);
    }

    @Test public void translatesNotes() {
        ParserTest.assertTranslatesTo("----", "<hr/>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("------", "<hr size=\"3\"/>" + HtmlElement.endl);
    }
}
