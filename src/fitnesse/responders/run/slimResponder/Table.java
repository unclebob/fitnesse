package fitnesse.responders.run.slimResponder;

import fitnesse.wikitext.widgets.*;
import fitnesse.wikitext.WikiWidget;
import fitnesse.util.StringUtil;

import java.util.List;
import java.util.regex.Matcher;

public class Table {
  private TableWidget widget;

  public Table(TableWidget widget) {
    this.widget = widget;
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    TextWidget textWidget = getCell(columnIndex, rowIndex);
    String cellText = textWidget.getText();
    return replaceLiterals(textWidget, cellText);
  }

  private String replaceLiterals(TextWidget textWidget, String cellText) {
    cellText = removeProcessedLiterals(textWidget, cellText);
    cellText = removeUnprocessedLiterals(cellText);
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

  private String removeProcessedLiterals(TextWidget textWidget, String cellText) {
    Matcher matcher = LiteralWidget.pattern.matcher(cellText);
    while (matcher.find()) {
      int literalNumber = Integer.parseInt(matcher.group(1));
      String replacement = textWidget.getParent().getLiteral(literalNumber);
      cellText = cellText.replace(matcher.group(), replacement);
      matcher = LiteralWidget.pattern.matcher(cellText);
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
      return("Could not convert table to string: " + e.getMessage());
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
    return getRowCount()-1;
  }
}
