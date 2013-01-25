// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.results.Result;
import fitnesse.testsystems.slim.tables.SyntaxError;

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
  void setCell(int col, int row, String content);

  int addRow(List<String> list);

  void appendContent(int row, String content);

  String getCellResult(int col,int row);

  void appendChildTable(int row, Table tableTemplate);

  @Deprecated
  void setTestStatusOnRow(int row, ExecutionResult testStatus);
  // FoxMe: setTestStatusOnRow becomes updateContent
  void updateContent(int row, Result result);

  @Deprecated
  void setCell(int col, int row, Result response);

  @Deprecated
  void appendContent(int col, int row, Result response);

  public Table asTemplate(CellContentSubstitution substitution) throws SyntaxError;

  interface CellContentSubstitution {
    String substitute(int col, int row, String content) throws SyntaxError;
  }
}
