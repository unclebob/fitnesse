package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class TableTest {

  @Test
  public void scansTables() {
    ParserTestHelper.assertScansTokenType("|a|\n", "Table", true);
    ParserTestHelper.assertScansTokenType("!|a|\n", "Table", true);
    ParserTestHelper.assertScansTokenType("-|a|\n", "Table", true);
    ParserTestHelper.assertScansTokenType("-!|a|\n", "Table", true);
    ParserTestHelper.assertScansTokenType("!|  a  |\n", "Table", true);
  }

  @Test
  public void parsesTables() {
    ParserTestHelper.assertParses("|a|\n", "SymbolList[Table[TableRow[TableCell[Text]]]]");
    ParserTestHelper.assertParses("!|a|\n", "SymbolList[Table[TableRow[TableCell[Text]]]]");
    ParserTestHelper.assertParses("|  a  |\n", "SymbolList[Table[TableRow[TableCell[Whitespace, Text, Whitespace]]]]");
    ParserTestHelper.assertParses("!|  a  |\n", "SymbolList[Table[TableRow[TableCell[Whitespace, Text, Whitespace]]]]");
    ParserTestHelper.assertParses("!|a:b|\n", "SymbolList[Table[TableRow[TableCell[Text, Colon, Text]]]]");
  }

  @Test
  public void translatesTables() {
    ParserTestHelper.assertTranslatesTo("|a|\n", tableWithCell("a"));
    ParserTestHelper.assertTranslatesTo("|a| \n", tableWithCell("a"));
    ParserTestHelper.assertTranslatesTo("|a|", tableWithCell("a"));
    ParserTestHelper.assertTranslatesTo("||\n", tableWithCell("", false));
    ParserTestHelper.assertTranslatesTo("| a |\n", tableWithCell("a"));
    ParserTestHelper.assertTranslatesTo("|!- a -!|\n", tableWithCell(" a "));
    ParserTestHelper.assertTranslatesTo("|''a''|\n", tableWithCell("<i>a</i>"));
    ParserTestHelper.assertTranslatesTo("|!c a|\n", tableWithCell("<center>a</center>"));
    ParserTestHelper.assertTranslatesTo("|http://mysite.org|\n",
            tableWithCell("<a href=\"http://mysite.org\">http://mysite.org</a>"));
    ParserTestHelper.assertTranslatesTo("|!-line\nbreaks\n-!|\n", tableWithCell("line\nbreaks\n"));

    ParserTestHelper.assertTranslatesTo("|a|b|c|\n|d|e|f|\n",
            "<table>" + HtmlElement.endl +
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
                    "</table>" + HtmlElement.endl);
  }

  @Test
  public void ignoresMalformedTables() {
    ParserTestHelper.assertTranslatesTo("!|\n\n|a|\n", "!|" + ParserTestHelper.newLineRendered + ParserTestHelper.newLineRendered + tableWithCell("a"));
  }

  @Test
  public void ignoreMostMarkupInLiteralTable() {
    ParserTestHelper.assertTranslatesTo("!|''<a''|\n", tableWithCell("''&lt;a''"));
    ParserTestHelper.assertTranslatesTo("!|a@b.com|\n", tableWithCell("a@b.com"));
  }

  @Test
  public void evaluatesExpressionsInLiteralTable() {
    ParserTestHelper.assertTranslatesTo("!|${=3+4=}|\n", tableWithCell("7"));
  }

  @Test
  public void normalizesRowLength() {
    ParserTestHelper.assertTranslatesTo("|a|\n|b|c|\n|d|e|f|\n",
            "<table>" + HtmlElement.endl +
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
                    "</table>" + HtmlElement.endl);
  }

  @Test
  public void hidesFirstRowInCommentTable() {
    ParserTestHelper.assertTranslatesTo("-|a|\n", tableWithCellAndRow("a", "<tr class=\"hidden\">"));
  }

  @Test
  public void combinesLiteralAndCommentOptions() {
    ParserTestHelper.assertTranslatesTo("-!|''<a''|\n", tableWithCellAndRow("''&lt;a''", "<tr class=\"hidden\">"));
  }

  @Test
  public void overridesNestedRule() {
    ParserTestHelper.assertTranslatesTo("|''|\n", tableWithCell("''", false));
    ParserTestHelper.assertTranslatesTo("|''a|\n''", tableWithCell("''a") + "''");
  }

  @Test
  public void translatesNestedLiteralTable() {
    ParserTestHelper.assertTranslatesTo("|${x}|\n", new TestVariableSource("x", "!|y|\n"),
            tableWithCell(ParserTestHelper.nestedTableWithCellAndRow("y", "<tr>")));
  }

  @Test
  public void translatesLiteralNestedTable() {
    ParserTestHelper.assertTranslatesTo("!|${x}|\n", new TestVariableSource("x", "|y|\n"), tableWithCell("|y|"));
  }

  @Test
  public void translatesVariableWithWhitespace() {
    ParserTestHelper.assertTranslatesTo("!|${x}|\n", new TestVariableSource("x", " a "), tableWithCell("a"));
    ParserTestHelper.assertTranslatesTo("!|${x}|\n", new TestVariableSource("x", "!- a -!"), tableWithCell(" a "));
    ParserTestHelper.assertTranslatesTo("!|${x}|\n${x}", new TestVariableSource("x", "!- a -!"), tableWithCell(" a ") + " a ");
  }

  private String tableWithCell(String cellContent, boolean hasRowTitle) {
    return ParserTestHelper.tableWithCell(cellContent, hasRowTitle);
  }

  private String tableWithCell(String cellContent) {
    return ParserTestHelper.tableWithCell(cellContent);
  }

  private String tableWithCellAndRow(String cellContent, String firstRow) {
    return ParserTestHelper.tableWithCellAndRow(cellContent, firstRow);
  }
}
