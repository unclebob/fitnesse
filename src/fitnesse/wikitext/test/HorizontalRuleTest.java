package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HorizontalRuleTest {
    @Test public void scansHorizontalRules() {
        ParserTestHelper.assertScansTokenType("----", "HorizontalRule", true);
        ParserTestHelper.assertScansTokenType("------", "HorizontalRule", true);
    }

    @Test public void translatesNotes() {
        ParserTestHelper.assertTranslatesTo("----", "<hr/>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("------", "<hr size=\"3\"/>" + HtmlElement.endl);
    }
}
