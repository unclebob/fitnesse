package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import org.junit.Test;


public class PlainTextTableTest {
    @Test
    public void scansPlainTextTables() {
        ParserTestHelper.assertScansTokenType("![\nstuff\n]!", "PlainTextTable", true);
        ParserTestHelper.assertScansTokenType("![\nstuff\n]!", "ClosePlainTextTable", true);
    }

    @Test public void parsesPlainTextTables() throws Exception {
        ParserTestHelper.assertParses("![\nstuff\n]!", "SymbolList[PlainTextTable[SymbolList[SymbolList[Text]]]]");
    }

    @Test public void translatesPlainTextTables() throws Exception {
        ParserTestHelper.assertTranslatesTo("![\nstuff\n]!",
          "<table class=\"plain_text_table\">" + HtmlElement.endl +
            "\t<tr>" + HtmlElement.endl +
            "\t\t<td>stuff</td>" + HtmlElement.endl +
            "\t</tr>" + HtmlElement.endl +
            "</table>" + HtmlElement.endl);
    }

    @Test public void hidesFirstRow() throws Exception {
        ParserTestHelper.assertTranslatesTo("![ stuff\n]!",
          "<table class=\"plain_text_table\">" + HtmlElement.endl +
            "\t<tr class=\"hidden\">" + HtmlElement.endl +
            "\t\t<td>stuff</td>" + HtmlElement.endl +
            "\t</tr>" + HtmlElement.endl +
            "</table>" + HtmlElement.endl);
    }

    @Test public void translatesDelimitedColumns() throws Exception {
        ParserTestHelper.assertTranslatesTo("![:\nstuff:nonsense\n]!",
          "<table class=\"plain_text_table\">" + HtmlElement.endl +
            "\t<tr>" + HtmlElement.endl +
            "\t\t<td>stuff</td>" + HtmlElement.endl +
            "\t\t<td>nonsense</td>" + HtmlElement.endl +
            "\t</tr>" + HtmlElement.endl +
            "</table>" + HtmlElement.endl);
    }
}
