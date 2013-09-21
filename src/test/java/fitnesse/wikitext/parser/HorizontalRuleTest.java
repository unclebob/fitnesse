package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HorizontalRuleTest {
    @Test public void scansHorizontalRules() {
        ParserTestHelper.assertScansTokenType("----", "HorizontalRule", true);
        ParserTestHelper.assertScansTokenType("------", "HorizontalRule", true);
    }

    @Test public void translatesHorizontalRules() {
        ParserTestHelper.assertTranslatesTo("----", "<hr/>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("------", "<hr size=\"3\"/>" + HtmlElement.endl);
        ParserTestHelper.assertTranslatesTo("----|a|",
                "<hr/>" + HtmlElement.endl + ParserTestHelper.tableWithCell("a"));
    }
}
