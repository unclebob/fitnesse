package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HashTableTokenTest {
    @Test public void scansHashTables() {
        ParserTest.assertScansTokenType("!{a:b,c:d}", SymbolType.HashTable, true);
        ParserTest.assertScansTokenType("!{a:b,c:d}", SymbolType.Colon, true);
        ParserTest.assertScansTokenType("!{a:b,c:d}", SymbolType.Comma, true);
    }

    @Test public void translatesHashTables() {
        ParserTest.assertTranslates("!{a:b,c:d}", hashTable());
        ParserTest.assertTranslates("!{a:b, c:d}", hashTable());
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
