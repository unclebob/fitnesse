package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
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
