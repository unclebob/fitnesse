// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.List;

import fitnesse.responders.run.ExecutionResult;
import fitnesse.responders.run.TestSummary;

public interface Table {
  String getCellContents(int columnIndex, int rowIndex);

  void appendToCell(int col, int row, String message);

  int getRowCount();

  int getColumnCountInRow(int rowIndex);

  String toString();

  String toHtml();

  void setCell(int col, int row, String contents);

  int addRow(List<String> list) throws Exception;

  void appendCellToRow(int row, String contents) throws Exception;

  String error(String s);

  String pass(String s);

  String fail(String s);

  String ignore(String s);

  String getUnescapedCellContents(int col, int row);

  String getCellResult(int col,int row);

  void appendChildTable(int row, Table table);

  void setTestStatusOnRow(int row, ExecutionResult testStatus);

  void setName(String tableName);
}
