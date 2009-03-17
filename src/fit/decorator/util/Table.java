package fit.decorator.util;

import java.text.ParseException;

import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;

public class Table {
  private final Parse table;

  public Table(Parse table) {
    this.table = table;
  }

  public Table(String html) throws ParseException {
    this.table = new Parse(html);
  }

  public Parse incrementColumnValues(int numberOfTimes, String columnName, Delta delta) throws InvalidInputException {
    int headerRowIndex = rowNumberContainingText(columnName);
    copyAndAppendLastRow(numberOfTimes - 1);
    incrementColumnValues(columnName, delta, headerRowIndex);
    return table;
  }

  public void insertAsFirstRow(Parse firstRow) {
    firstRow.more = table.parts;
    table.parts = firstRow;
  }

  public Parse stripFirstRow() {
    Parse firstRow = table.parts;
    table.parts = table.parts.more;
    return firstRow;
  }

  public String toString() {
    return toSimpleText(table, new StringBuffer());
  }

  int columnNumberContainingText(String columnName, int headerRowIndex) throws InvalidInputException {
    int columnNumber = -1;
    Parse columns = table.at(0, headerRowIndex, 0);
    while (columns != null) {
      columnNumber++;
      if (columnName.equals(columns.text())) {
        return columnNumber;
      }
      columns = columns.more;
    }
    throw new InvalidInputException(errorMsg(columnName));
  }

  String columnValue(int rowIndex, int columnIndex) {
    return table.at(0, rowIndex, columnIndex).text();
  }

  public void copyAndAppendLastRow(int numberOfTimes) {
    if (numberOfTimes > 0 && tableHasMoreThanTwoRows()) {
      Parse lastRow = lastRow();
      Parse secondLastRow = secondLastRow(lastRow);
      copyAndAppend(lastRow, numberOfTimes);
      secondLastRow.more = lastRow;
    }
  }

  void incrementColumnValues(String columnName, Delta delta, int headerRowIndex) throws InvalidInputException {
    int columnNumber = columnNumberContainingText(columnName, headerRowIndex);
    int totalNumberOfRows = numberOfRows();
    for (int i = headerRowIndex + 2; i < totalNumberOfRows; ++i) {
      Parse columnToBeUpdated = table.at(0, i, columnNumber);
      String value = columnToBeUpdated.text();
      value = delta.addTo(value, i - headerRowIndex - 1);
      columnToBeUpdated.body = value;
    }
  }

  Parse lastRow() {
    return table.parts.last();
  }

  int numberOfRows() {
    return table.parts.size();
  }

  int rowNumberContainingText(String searchText) throws InvalidInputException {
    Parse rows = table.at(0, 0);
    int numberOfRows = rows.size();
    for (int i = 0; i < numberOfRows; i++) {
      Parse columns = table.at(0, i, 0);
      int numberOfColumns = columns.size();
      for (int j = 0; j < numberOfColumns; ++j) {
        if (searchText.equals(table.at(0, i, j).text())) {
          return i;
        }
      }
    }
    throw new InvalidInputException(errorMsg(searchText));
  }

  Parse secondLastRow(Parse lastRow) {
    Parse nextRow = table.parts;
    Parse currentRow = null;
    while (nextRow != lastRow) {
      currentRow = nextRow;
      nextRow = nextRow.more;
    }
    currentRow.more = null;
    return currentRow;
  }

  private void copyAndAppend(Parse lastRow, int numberOfTimes) {
    for (int i = 0; i < numberOfTimes; i++) {
      Parse columns = lastRow.parts;
      Parse nextColumn = columns.more;
      Parse newNextColumn = newParse(nextColumn, nextColumn.more);
      Parse newColumn = newParse(columns, newNextColumn);
      Parse newRow = new Parse(stripAngularBrackets(lastRow.tag), lastRow.body, newColumn, null);
      lastRow.last().more = newRow;
    }
  }

  private Parse newParse(Parse columns, Parse nextColumn) {
    return new Parse(stripAngularBrackets(columns.tag), columns.body, columns.parts, nextColumn);
  }

  private String errorMsg(String searchText) {
    return "'" + searchText + "' was not found in the table " + toString();
  }

  private void simpleTextOfLeave(Parse table, StringBuffer returnText) {
    returnText.append(table.tag).append(table.text()).append(table.end);
  }

  private void simpleTextOfMore(Parse table, StringBuffer returnText) {
    if ((table.more != null)) {
      toSimpleText(table.more, returnText);
    }
  }

  private void simpleTextOfParts(Parse table, StringBuffer returnText) {
    returnText.append(table.tag);
    toSimpleText(table.parts, returnText);
    returnText.append(table.end);
  }

  private String stripAngularBrackets(String tag) {
    return tag.substring(1, tag.length() - 1);
  }

  private String toSimpleText(Parse table, StringBuffer returnText) {
    if (table.parts == null) {
      simpleTextOfLeave(table, returnText);
      simpleTextOfMore(table, returnText);
      return returnText.toString();
    }
    simpleTextOfParts(table, returnText);
    simpleTextOfMore(table, returnText);
    return returnText.toString();
  }

  public Parse table() {
    return table;
  }

  public Parse incrementColumnValuesByDelta(String columnName, Delta delta) throws InvalidInputException {
    int headerRowIndex = rowNumberContainingText(columnName);
    incrementColumnValues(columnName, delta, headerRowIndex);
    return table;
  }

  private boolean tableHasMoreThanTwoRows() {
    return (table.parts.size() > 2);
  }
}
