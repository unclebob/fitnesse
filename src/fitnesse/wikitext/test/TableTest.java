package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import org.junit.Test;

public class TableTest {
    @Test public void scansTables() {
        ParserTest.assertScans("|a|\n", "Table=|,Text=a,EndCell=|\n");
        ParserTest.assertScans("!|a|\n", "Table=!|,Text=a,EndCell=|\n");
    }

    @Test public void translatesTables() {
        ParserTest.assertTranslatesTo("|a|\n", tableWithCell("a"));
        ParserTest.assertTranslatesTo("||\n", tableWithCell(""));
        ParserTest.assertTranslatesTo("| a |\n", tableWithCell("a"));
        ParserTest.assertTranslatesTo("|''a''|\n", tableWithCell("<i>a</i>"));
        ParserTest.assertTranslatesTo("!|''a''|\n", tableWithCell("''a''"));

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

    private String tableWithCell(String cellContent) {
        return "<table border=\"1\" cellspacing=\"0\">"+ HtmlElement.endl +
        "\t<tr>" + HtmlElement.endl +
        "\t\t<td>" + cellContent + "</td>" + HtmlElement.endl +
        "\t</tr>" + HtmlElement.endl +
        "</table>"+ HtmlElement.endl;
    }
}
