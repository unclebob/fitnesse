package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HorizontalRuleTokenTest {
    @Test public void scansHorizontalRules() {
        ParserTest.assertScansTokenType("----", TokenType.HorizontalRule, true);
        ParserTest.assertScansTokenType("------", TokenType.HorizontalRule, true);
    }

    @Test public void translatesNotes() {
        ParserTest.assertTranslates("----", "<hr/>" + HtmlElement.endl);
        ParserTest.assertTranslates("------", "<hr size=\"3\"/>" + HtmlElement.endl);
    }
}
