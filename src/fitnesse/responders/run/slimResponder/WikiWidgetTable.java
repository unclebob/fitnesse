package fitnesse.responders.run.slimResponder;

import fitnesse.util.StringUtil;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.*;

import java.util.List;
import java.util.regex.Matcher;

public class WikiWidgetTable implements Table {
  private TableWidget widget;

  public WikiWidgetTable(TableWidget widget) {
    this.widget = widget;
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    TextWidget textWidget = getCell(columnIndex, rowIndex);
    String cellText = textWidget.getText();
    return replaceLiteralsAndVariables(textWidget, cellText);
  }

  private String replaceLiteralsAndVariables(TextWidget textWidget, String cellText) {
    cellText = removeUnprocessedLiterals(cellText);
    cellText = replaceVariables(textWidget, cellText);
    return cellText;
  }

  private String replaceVariables(WikiWidget widget, String cellText) {
    Matcher matcher = VariableWidget.pattern.matcher(cellText);
    while (matcher.find()) {
      String replacement = null;
      try {
        replacement = widget.getWikiPage().getData().getVariable(matcher.group(1));
      } catch (Exception e) {
        replacement = null;
      }
      if (replacement != null)
        cellText = cellText.replace(matcher.group(), replacement);
    }
    return cellText;
  }

  private static String removeUnprocessedLiterals(String cellText) {
    Matcher matcher = PreProcessorLiteralWidget.pattern.matcher(cellText);
    while (matcher.find()) {
      String replacement = matcher.group(1);
      cellText = cellText.replace(matcher.group(), replacement);
    }
    return cellText;
  }

  protected TextWidget getCell(int columnIndex, int rowIndex) {
    TableRowWidget row = getRow(rowIndex);
    TextWidget cellContents = getCellInRow(row, columnIndex);
    return cellContents;
  }

  public void appendToCell(int col, int row, String message) {
    TextWidget widget = getCell(col, row);
    widget.setText(widget.getText() + " " + message);
  }

  private TextWidget getCellInRow(TableRowWidget row, int columnIndex) {
    List<WikiWidget> columns = row.getChildren();
    TableCellWidget cell = (TableCellWidget) columns.get(columnIndex);
    TextWidget cellContents = (TextWidget) cell.getChildren().get(0);
    return cellContents;
  }

  private TableRowWidget getRow(int rowIndex) {
    List<WikiWidget> rows = widget.getChildren();
    TableRowWidget row = (TableRowWidget) rows.get(rowIndex);
    return row;
  }

  public int getRowCount() {
    return widget.getChildren().size();
  }

  public int getColumnCountInRow(int rowIndex) {
    TableRowWidget row = getRow(rowIndex);
    return row.getChildren().size();
  }

  public String toString() {
    try {
      return widget.asWikiText();
    } catch (Exception e) {
      return ("Could not convert table to string: " + e.getMessage());
    }
  }

  public void setCell(int col, int row, String contents) {
    TextWidget textWidget = getCell(col, row);
    textWidget.setText(contents);
  }

  public void setAsNotLiteralTable() {
    widget.setLiteralTable(false);
  }

  public boolean isLiteralTable() {
    return widget.isLiteralTable;
  }


  public int addRow(List<String> list) throws Exception {
    String rowString = "|" + StringUtil.join(list, "|") + "|\n";
    widget.addRows(rowString);
    return getRowCount() - 1;
  }

  public void appendCellToRow(int row, String contents) throws Exception {
    int lastCol = getColumnCountInRow(row) - 1;
    TextWidget textWidget = getCell(lastCol, row);
    TableRowWidget rowWidget = (TableRowWidget) textWidget.getParent().getParent();
    rowWidget.addCells(contents);
  }
}
