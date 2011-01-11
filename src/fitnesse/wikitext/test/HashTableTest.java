package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HashTableTest {
    @Test public void scansHashTables() {
        ParserTestHelper.assertScansTokenType("!{a:b,c:d}", "HashTable", true);
        ParserTestHelper.assertScansTokenType("!{a:b,c:d}", "Colon", true);
        ParserTestHelper.assertScansTokenType("!{a:b,c:d}", "Comma", true);
    }

    @Test public void translatesHashTables() {
        ParserTestHelper.assertTranslatesTo("!{a:b,c:d}", hashTable());
        ParserTestHelper.assertTranslatesTo("!{a:b, c:d}", hashTable());
    }

    private String hashTable() {
        return "<table class=\"hash_table\">" + HtmlElement.endl +
        "\t<tr class=\"hash_row\">" + HtmlElement.endl +
        "\t\t<td class=\"hash_key\">a</td>" + HtmlElement.endl +
        "\t\t<td class=\"hash_value\">b</td>" + HtmlElement.endl +
        "\t</tr>" + HtmlElement.endl +
        "\t<tr class=\"hash_row\">" + HtmlElement.endl +
        "\t\t<td class=\"hash_key\">c</td>" + HtmlElement.endl +
        "\t\t<td class=\"hash_value\">d</td>" + HtmlElement.endl +
        "\t</tr>" + HtmlElement.endl +
        "</table>" + HtmlElement.endl;
    }
}
