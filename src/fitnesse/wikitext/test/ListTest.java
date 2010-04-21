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
        ParserTest.assertTranslatesTo(" * item1\n * item2\n",
                list("ul", 0) + listItem("item1", 1) + listItem("item2", 1) + list("/ul", 0));
    }

    private String list(String tag, int level) {
        return indent(level) + "<" + tag  + ">" + HtmlElement.endl;
    }


    private String listItem(String item, int level) {
        return indent(level) + "<li>" + item + "</li>" + HtmlElement.endl;
    }

    private String indent(int level) {
        String result = "";
        for (int i = 0; i < level; i++) result += "\t";
        return result;
    }

    @Test
    public void translatesNestedLists() {
        ParserTest.assertTranslatesTo(" * item1\n  * item2\n  * item3\n",
                list("ul", 0) +
                listItem("item1", 1) +
                listItem(list("ul", 0) + listItem("item2", 1) + listItem("item3", 1) + list("/ul", 0) ,1) +
                list("/ul", 0));
    }
    
    @Test
    public void translatesOrderedLists() {
        ParserTest.assertTranslatesTo(" 1 item1\n 2 item2\n",
                list("ol", 0) + listItem("item1", 1) + listItem("item2", 1) + list("/ol", 0));
    }
}
