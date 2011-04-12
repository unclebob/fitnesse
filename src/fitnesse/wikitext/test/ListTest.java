package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class ListTest {
    @Test
    public void scansLists() {
        ParserTestHelper.assertScansTokenType(" * item", "UnorderedList", true);
        ParserTestHelper.assertScansTokenType(" *item", "UnorderedList", true);
        ParserTestHelper.assertScansTokenType("  * item", "UnorderedList", true);
        ParserTestHelper.assertScansTokenType("* item", "UnorderedList", false);
        ParserTestHelper.assertScansTokenType(" 1 item", "OrderedList", true);
        ParserTestHelper.assertScansTokenType("  9 item", "OrderedList", true);
        ParserTestHelper.assertScansTokenType("1 item", "OrderedList", false);
    }

    @Test
    public void translatesUnorderedLists() {
        ParserTestHelper.assertTranslatesTo(" * item1\n * item2\n",
          list("ul", 0) + listItem("item1", 1) + listItem("item2", 1) + list("/ul", 0));
    }

    @Test
    public void overridesNestedPairRule() {
        ParserTestHelper.assertTranslatesTo(" * item--1\n--",
          list("ul", 0) + listItem("item--1", 1) + list("/ul", 0) + "--");
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
        ParserTestHelper.assertTranslatesTo(" * item1\n  * item2\n  * item3\n",
          list("ul", 0) +
            listItem("item1" + list("ul", 0) + listItem("item2", 1) + listItem("item3", 1) + list("/ul", 0), 1) +
            list("/ul", 0));
    }

    @Test
    public void translatesMultipleNestedLists() {
        ParserTestHelper.assertTranslatesTo(" * item1\n  * item11\n * item2\n  * item21\n",
          list("ul", 0) +
                  listItem("item1" + list("ul", 0) + listItem("item11", 1) + list("/ul", 0), 1) +
                  listItem("item2" + list("ul", 0) + listItem("item21", 1) + list("/ul", 0), 1) +
             list("/ul", 0));
    }

    @Test
    public void translatesOrderedLists() {
        ParserTestHelper.assertTranslatesTo(" 1 item1\n 2 item2\n",
          list("ol", 0) + listItem("item1", 1) + listItem("item2", 1) + list("/ol", 0));
    }
}
