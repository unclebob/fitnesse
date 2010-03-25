package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class ListTokenTest {
    @Test
    public void scansLists() {
        ParserTest.assertScansTokenType(" * item", TokenType.List, true);
        ParserTest.assertScansTokenType("  * item", TokenType.List, true);
        ParserTest.assertScansTokenType("* item", TokenType.List, false);
    }

    @Test
    public void translatesLists() {
        ParserTest.assertTranslates(" * item\n",
                "<ul>" + HtmlElement.endl +
                "\t<li> item</li>" + HtmlElement.endl +
                "</ul>" + HtmlElement.endl);
    }
}
