package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class ListTest {
    @Test
    public void scansLists() {
        ParserTest.assertScansTokenType(" * item", SymbolType.UnorderedList, true);
        ParserTest.assertScansTokenType("  * item", SymbolType.UnorderedList, true);
        ParserTest.assertScansTokenType("* item", SymbolType.UnorderedList, false);
        ParserTest.assertScansTokenType(" 1 item", SymbolType.OrderedList, true);
        ParserTest.assertScansTokenType("  9 item", SymbolType.OrderedList, true);
        ParserTest.assertScansTokenType("1 item", SymbolType.OrderedList, false);
    }

    @Test
    public void translatesUnorderedLists() {
        ParserTest.assertTranslatesTo(" * item1\n * item2\nmore",
                "<ul>" + HtmlElement.endl +
                "\t<li>item1</li>" + HtmlElement.endl +
                "\t<li>item2</li>" + HtmlElement.endl +
                "</ul>" + HtmlElement.endl + "more");
    }
    @Test
    public void translatesOrderedLists() {
        ParserTest.assertTranslatesTo(" 1 item1\n 2 item2\nmore",
                "<ol>" + HtmlElement.endl +
                "\t<li>item1</li>" + HtmlElement.endl +
                "\t<li>item2</li>" + HtmlElement.endl +
                "</ol>" + HtmlElement.endl + "more");
    }
}
