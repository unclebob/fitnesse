package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;


public class PlainTextTableTest {
    @Test
    public void scansPlainTextTables() {
        ParserTest.assertScansTokenType("![\nstuff\n]!", SymbolType.PlainTextTable, true);
        ParserTest.assertScansTokenType("![\nstuff\n]!", SymbolType.ClosePlainTextTable, true);
    }

    @Test public void parsesPlainTextTables() throws Exception {
        ParserTest.assertParses("![\nstuff\n]!", "SymbolList[PlainTextTable[SymbolList[SymbolList[Text]]]]");
    }

    @Test public void translatesPlainTextTables() throws Exception {
        ParserTest.assertTranslatesTo("![\nstuff\n]!",
                "<table class=\"plain_text_table\">" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>stuff</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "</table>" + HtmlElement.endl);
    }

    @Test public void hidesFirstRow() throws Exception {
        ParserTest.assertTranslatesTo("![ stuff\n]!",
                "<table class=\"plain_text_table\">" + HtmlElement.endl +
                "\t<tr class=\"hidden\">" + HtmlElement.endl +
                "\t\t<td>stuff</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "</table>" + HtmlElement.endl);
    }

    @Test public void translatesDelimitedColumns() throws Exception {
        ParserTest.assertTranslatesTo("![:\nstuff:nonsense\n]!",
                "<table class=\"plain_text_table\">" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>stuff</td>" + HtmlElement.endl +
                "\t\t<td>nonsense</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "</table>" + HtmlElement.endl);
    }
}
