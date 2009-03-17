package fit.decorator.util;

import junit.framework.TestCase;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;

public class TableTest extends TestCase {
  private String fitPage = "<table><tr><td>eg.Division</td></tr>"
    + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
    + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
  private Table table;

  protected void setUp() throws Exception {
    super.setUp();
    table = new Table(fitPage);
  }

  public void testToStringShouldParseTableWithMultipleRowsAndColumns() throws Exception {
    assertTable(fitPage);
  }

  public void testConstructorShouldBuildTableFromParseObject() throws Exception {
    String expectedTableContents = "<tr><td>fit.decorator.MaxTime</td><td>10</td></tr>";
    Parse parse = new Parse(expectedTableContents, new String[]
      {"tr", "td"});
    table = new Table(parse);
    assertTable(expectedTableContents);
  }

  public void testCopyAndAppendLastRow() throws Exception {
    table.copyAndAppendLastRow(1);
    String expectedTableContents = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr>" + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    assertTable(expectedTableContents);
  }

  public void testCopyAndAppendLastRowMultipleTimes() throws Exception {
    table.copyAndAppendLastRow(3);
    String expectedTableContents = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>10</td><td>2</td><td>5</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr>" + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    assertTable(expectedTableContents);
  }

  public void testCopyAndAppendLastRowZeroTime() throws Exception {
    table.copyAndAppendLastRow(0);
    assertTable(fitPage);
  }

  public void testIncrementColumnValueThrowsInvalidInputExceptionIfColumnNameIsNotFound() throws Exception {
    try {
      table.incrementColumnValues(1, "invalidColumnName", null);
    } catch (InvalidInputException e) {
      assertEquals("'invalidColumnName' was not found in the table " + fitPage, e.getMessage());
    }
  }

  public void testIncrementColumnValueShouldAddTheGivenDeltaToAllRowsOfTheGivenColumn() throws Exception {
    table.incrementColumnValues("denominator", new Delta("int", "1"), 1);
    assertTable(fitPage);
  }

  public void testIncrementColumnValueShouldIncrementallyAddTheGivenDeltaMultipleTimes() throws Exception {
    table.incrementColumnValues(3, "denominator", new Delta("int", "1"));
    String expectedFitPage = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>10</td><td>3</td><td>5</td></tr>"
      + "<tr><td>10</td><td>4</td><td>5</td></tr></table>";
    assertTable(expectedFitPage);
  }

  public void testInsertAsFirstRow() throws Exception {
    Parse firstRow = new Parse("<tr><td>first row</td></tr>", new String[]
      {"tr", "td"});
    table.insertAsFirstRow(firstRow);
    String expectedTableContents = "<table><tr><td>first row</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    assertTable(expectedTableContents);
  }

  public void testStripFirstRow() throws Exception {
    table.stripFirstRow();
    String expectedTableContents = "<table>" + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    assertTable(expectedTableContents);
  }

  public void testColumnNumberShouldReturnColumnNumberForTheGivenColumnName() throws Exception {
    assertEquals(0, table.columnNumberContainingText("numerator", 1));
    assertEquals(1, table.columnNumberContainingText("denominator", 1));
    assertEquals(2, table.columnNumberContainingText("quotient()", 1));
  }

  public void testColumnNumberShouldThrowsInvalidInputExceptionIfColumnNameIsNotFound() throws Exception {
    try {
      table.columnNumberContainingText("invalidColumnName", 1);
    } catch (InvalidInputException e) {
      assertEquals("'invalidColumnName' was not found in the table " + fitPage, e.getMessage());
    }
  }

  public void testColumnValueShouldReturnColumnValueForTheGivenColumnNumber() throws Exception {
    assertEquals("10", table.columnValue(2, 0));
    assertEquals("2", table.columnValue(2, 1));
    assertEquals("5", table.columnValue(2, 2));
  }

  public void testGetLastRow() throws Exception {
    Parse lastRow = table.lastRow();
    String expectedLastRow = "<tr><td>10</td><td>2</td><td>5</td></tr>";
    assertParseObject(expectedLastRow, lastRow);
  }

  public void testGetSecondLastRow() throws Exception {
    String fitPage = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr>" + "<tr><td>5</td><td>1</td><td>5</td></tr></table>";
    table = new Table(fitPage);
    Parse secondLastRow = table.secondLastRow(table.lastRow());
    String expectedSecondLastRow = "<tr><td>10</td><td>2</td><td>5</td></tr>";
    assertParseObject(expectedSecondLastRow, secondLastRow);
  }

  public void testRowNumberContainingTextShouldReturnIndexOfTheRowContainingTheText() throws Exception {
    assertEquals(0, table.rowNumberContainingText("eg.Division"));
    assertEquals(1, table.rowNumberContainingText("numerator"));
    assertEquals(1, table.rowNumberContainingText("denominator"));
  }

  public void testRowNumberContainingTextShouldThrowInvalidInputExceptionIfSearchTextIsNotFound() throws Exception {
    try {
      table.rowNumberContainingText("invalidColumnName");
    } catch (InvalidInputException e) {
      assertEquals("'invalidColumnName' was not found in the table " + fitPage, e.getMessage());
    }
  }

  public void testCopyAndAppendLastRowShouldLeaveTheTableAsItIsIfTotalRowsAreLessThanThree() throws Exception {
    String fitPage = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr></table>";
    table = new Table(fitPage);
    table.copyAndAppendLastRow(4);
    assertTable(fitPage);
  }

  private void assertTable(String expectedTableContents) {
    assertEquals(expectedTableContents, table.toString());
  }

  private void assertParseObject(String expectedTableContents, Parse parse) {
    assertEquals(expectedTableContents, new Table(parse).toString());
  }
}
