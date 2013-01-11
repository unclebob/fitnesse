// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.List;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.results.Result;

public interface Table {
  String getCellContents(int columnIndex, int rowIndex);

  void appendToCell(int col, int row, String message);

  int getRowCount();

  int getColumnCountInRow(int rowIndex);

  String toString();

  String toHtml();

  void setCell(int col, int row, String contents);

  int addRow(List<String> list);

  void appendCellToRow(int row, String contents);

  String getUnescapedCellContents(int col, int row);

  String getCellResult(int col,int row);

  void appendChildTable(int row, Table table);

  void setTestStatusOnRow(int row, ExecutionResult testStatus);

  void setName(String tableName);

  void setCell(int col, int row, Result response);

  void appendToCell(int col, int row, Result response);
}
