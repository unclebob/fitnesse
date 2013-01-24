// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.results.Result;

import java.util.List;

public interface Table {
  String getCellContents(int col, int row);
  // FixMe: -AJM- Can I remove this without functional change???
  String getUnescapedCellContents(int col, int row);


  @Deprecated
  void appendToCell(int col, int row, String message);

  int getRowCount();

  int getColumnCountInRow(int row);

  @Deprecated
  void setCell(int col, int row, String contents);

  int addRow(List<String> list);

  void appendContent(int row, String contents);

  String getCellResult(int col,int row);

  void appendChildTable(int row, Table table);

  @Deprecated
  void setTestStatusOnRow(int row, ExecutionResult testStatus);
  // FoxMe: setTestStatusOnRow becomes updateContent
  void updateContent(int row, Result result);

  @Deprecated
  void setCell(int col, int row, Result response);

  @Deprecated
  void appendContent(int col, int row, Result response);
}
