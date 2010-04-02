package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class ListTest {
    @Test
    public void scansLists() {
        ParserTest.assertScansTokenType(" * item", SymbolType.List, true);
        ParserTest.assertScansTokenType("  * item", SymbolType.List, true);
        ParserTest.assertScansTokenType("* item", SymbolType.List, false);
    }

    @Test
    public void translatesLists() {
        ParserTest.assertTranslatesTo(" * item\n",
                "<ul>" + HtmlElement.endl +
                "\t<li> item</li>" + HtmlElement.endl +
                "</ul>" + HtmlElement.endl);
    }
}
