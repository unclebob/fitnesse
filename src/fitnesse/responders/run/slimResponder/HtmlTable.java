package fitnesse.responders.run.slimResponder;

import org.htmlparser.Node;
import org.htmlparser.tags.*;
import org.htmlparser.util.NodeList;

import java.util.ArrayList;
import java.util.List;

public class HtmlTable implements Table {
  private List<Row> rows = new ArrayList<Row>();

  public HtmlTable(TableTag tableTag) {
    NodeList nodeList = tableTag.getChildren();
    for (int i = 0; i < nodeList.size(); i++) {
      Node node = nodeList.elementAt(i);
      if (node instanceof TableRow || node instanceof TableHeader) {
        rows.add(new Row((CompositeTag) node));
      }
    }
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    return rows.get(rowIndex).getColumn(columnIndex).getContent();
  }

  public void appendToCell(int col, int row, String message) {
  }

  public int getRowCount() {
    return rows.size();
  }

  public int getColumnCountInRow(int rowIndex) {
    return rows.get(rowIndex).getColumnCount();
  }

  public void setCell(int col, int row, String contents) {
  }

  public int addRow(List<String> list) throws Exception {
    return 0;
  }

  public void appendCellToRow(int row, String contents) throws Exception {
  }
}


class Row {
  private List<Column> columns = new ArrayList<Column>();

  public Row(CompositeTag tableRow) {
    NodeList nodeList = tableRow.getChildren();
    for (int i = 0; i < nodeList.size(); i++) {
      Node node = nodeList.elementAt(i);
      if (node instanceof TableColumn)
        columns.add(new Column((TableColumn) node));
    }

  }

  public int getColumnCount() {
    return columns.size();
  }

  public Column getColumn(int columnIndex) {
    return columns.get(columnIndex);
  }
}

class Column {
  private TableColumn columnNode;

  public Column(TableColumn tableColumn) {
    columnNode = tableColumn;
  }

  public String getContent() {
    return columnNode.getChildrenHTML();
  }
}
