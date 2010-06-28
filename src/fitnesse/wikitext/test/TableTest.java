package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class TableTest {
    @Test public void scansTables() {
        ParserTest.assertScansTokenType("|a|\n", "Table", true);
        ParserTest.assertScansTokenType("!|a|\n", "Table", true);
        ParserTest.assertScansTokenType("-|a|\n", "Table", true);
        ParserTest.assertScansTokenType("-!|a|\n", "Table", true);
    }

    @Test public void translatesTables() {
        ParserTest.assertTranslatesTo("|a|\n", tableWithCell("a"));
        ParserTest.assertTranslatesTo("|a|", tableWithCell("a"));
        ParserTest.assertTranslatesTo("||\n", tableWithCell(""));
        ParserTest.assertTranslatesTo("| a |\n", tableWithCell("a"));
        ParserTest.assertTranslatesTo("|''a''|\n", tableWithCell("<i>a</i>"));
        ParserTest.assertTranslatesTo("|!c a|\n", tableWithCell("<div class=\"centered\">a</div>"));
        ParserTest.assertTranslatesTo("|!c a|\n", tableWithCell("<div class=\"centered\">a</div>"));
        ParserTest.assertTranslatesTo("|http://mysite.org|\n",
                tableWithCell("<a href=\"http://mysite.org\">http://mysite.org</a>"));
        ParserTest.assertTranslatesTo("|!-line\nbreaks\n-!|\n", tableWithCell("line\nbreaks"));

        ParserTest.assertTranslatesTo("|a|b|c|\n|d|e|f|\n",
                "<table border=\"1\" cellspacing=\"0\">" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>a</td>" + HtmlElement.endl +
                "\t\t<td>b</td>" + HtmlElement.endl +
                "\t\t<td>c</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>d</td>" + HtmlElement.endl +
                "\t\t<td>e</td>" + HtmlElement.endl +
                "\t\t<td>f</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "</table>"+ HtmlElement.endl);
    }

    @Test public void ignoreMostMarkupInLiteralTable() {
        ParserTest.assertTranslatesTo("!|''<a''|\n", tableWithCell("''&lt;a''"));
    }

    @Test public void evaluatesExpressionsInLiteralTable() {
        ParserTest.assertTranslatesTo("!|${=3+4=}|\n", tableWithCell("7"));
    }

    @Test public void normalizesRowLength() {
        ParserTest.assertTranslatesTo("|a|\n|b|c|\n|d|e|f|\n",
                "<table border=\"1\" cellspacing=\"0\">" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td colspan=\"3\">a</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>b</td>" + HtmlElement.endl +
                "\t\t<td colspan=\"2\">c</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "\t<tr>" + HtmlElement.endl +
                "\t\t<td>d</td>" + HtmlElement.endl +
                "\t\t<td>e</td>" + HtmlElement.endl +
                "\t\t<td>f</td>" + HtmlElement.endl +
                "\t</tr>" + HtmlElement.endl +
                "</table>"+ HtmlElement.endl);
    }

    @Test public void hidesFirstRowInCommentTable() {
        ParserTest.assertTranslatesTo("-|a|\n", tableWithCellAndRow("a", "<tr class=\"hidden\">"));
    }
    @Test public void combinesLiteralAndCommentOptions() {
        ParserTest.assertTranslatesTo("-!|''<a''|\n", tableWithCellAndRow("''&lt;a''", "<tr class=\"hidden\">"));
    }

    private String tableWithCell(String cellContent) {
        return tableWithCellAndRow(cellContent, "<tr>");
    }

    private String tableWithCellAndRow(String cellContent, String firstRow) {
        return "<table border=\"1\" cellspacing=\"0\">"+ HtmlElement.endl +
        "\t" + firstRow + HtmlElement.endl +
        "\t\t<td>" + cellContent + "</td>" + HtmlElement.endl +
        "\t</tr>" + HtmlElement.endl +
        "</table>"+ HtmlElement.endl;
    }
}
