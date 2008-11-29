package fitnesse.responders.run.slimResponder;

import java.util.List;

public interface Table {
  String getCellContents(int columnIndex, int rowIndex);

  void appendToCell(int col, int row, String message);

  int getRowCount();

  int getColumnCountInRow(int rowIndex);

  String toString();

  void setCell(int col, int row, String contents);

  int addRow(List<String> list) throws Exception;

  void appendCellToRow(int row, String contents) throws Exception;
}
