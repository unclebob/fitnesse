package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HorizontalRuleTest {
    @Test public void scansHorizontalRules() {
        ParserTest.assertScansTokenType("----", SymbolType.HorizontalRule, true);
        ParserTest.assertScansTokenType("------", SymbolType.HorizontalRule, true);
    }

    @Test public void translatesNotes() {
        ParserTest.assertTranslatesTo("----", "<hr/>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("------", "<hr size=\"3\"/>" + HtmlElement.endl);
    }
}
