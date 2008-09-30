package fitnesse.responders.run.slimResponder;

import fitnesse.wikitext.widgets.TableWidget;
import fitnesse.wikitext.widgets.TableRowWidget;
import fitnesse.wikitext.widgets.TextWidget;
import fitnesse.wikitext.widgets.TableCellWidget;
import fitnesse.wikitext.WikiWidget;

import java.util.List;

public class Table {
  private TableWidget widget;

  public Table(TableWidget widget) {
    this.widget = widget;
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    return getCell(columnIndex, rowIndex).getText();
  }

  private TextWidget getCell(int columnIndex, int rowIndex) {
    TableRowWidget row = getRow(rowIndex);
    TextWidget cellContents = getCellInRow(row, columnIndex);
    return cellContents;
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
}
