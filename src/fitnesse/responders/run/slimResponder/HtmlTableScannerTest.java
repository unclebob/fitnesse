package fitnesse.responders.run.slimResponder;

import org.htmlparser.util.ParserException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HtmlTableScannerTest {
  private HtmlTableScanner ts;

  private void scan(String page) throws ParserException {
    ts = new HtmlTableScanner(page);
  }

  @Test
  public void canHandleEmptyInput() throws Exception {
    scan("");
    assertEquals(0, ts.getTableCount());
  }

  @Test
  public void canParseOneSimpleTable() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(1, t.getRowCount());
    assertEquals(1, t.getColumnCountInRow(0));
    assertEquals("x", t.getCellContents(0, 0));
  }


  @Test
  public void canParseOneSimpleTableWithGunkAroundIt() throws Exception {
    scan(
      "" +
        "<body>Gunk" +
        "<table>gunk" +
        "  <tr> gunk" +
        "     <td>x</td>gunk" +
        "   </tr>gunk" +
        "</table>gunk" +
        "</body>");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(1, t.getRowCount());
    assertEquals(1, t.getColumnCountInRow(0));
    assertEquals("x", t.getCellContents(0, 0));
  }

  @Test
  public void canParseComplexTable() throws Exception {
    scan(
      "  <table>" +
        "  <th>" +
        "    <td>Name</td>" +
        "    <td>Address</td>" +
        "  </th>" +
        "  <tr>" +
        "    <td>Bob</td>" +
        "    <td>Here</td>" +
        "  </tr>" +
        "</table>  ");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(2, t.getRowCount());
    assertEquals(2, t.getColumnCountInRow(0));
    assertEquals(2, t.getColumnCountInRow(1));
    assertEquals("Name", t.getCellContents(0, 0));
    assertEquals("Address", t.getCellContents(1, 0));
    assertEquals("Bob", t.getCellContents(0, 1));
    assertEquals("Here", t.getCellContents(1, 1));
  }

  @Test
  public void canParseMultipleTables() throws Exception {
    scan(
      "" +
        "<table><tr><td>1</td></tr></table>" +
        "<table><tr><td>2</td></tr></table>"
    );

    assertEquals(2, ts.getTableCount());
    Table t1 = ts.getTable(0);
    Table t2 = ts.getTable(1);
    assertEquals(1, t1.getRowCount());
    assertEquals(1, t2.getRowCount());
    assertEquals(1, t1.getColumnCountInRow(0));
    assertEquals(1, t2.getColumnCountInRow(0));
    assertEquals("1", t1.getCellContents(0, 0));
    assertEquals("2", t2.getCellContents(0, 0));
  }

  @Test
  public void canConvertToWiki() throws Exception {
      scan(
      "  <table>" +
        "  <th>" +
        "    <td>Name</td>" +
        "    <td>Address</td>" +
        "  </th>" +
        "  <tr>" +
        "    <td>Bob</td>" +
        "    <td>Here</td>" +
        "  </tr>" +
        "</table>  ");
    assertEquals("\n|Name|Address|\n|Bob|Here|\n", ts.toWikiText());
  }
}
