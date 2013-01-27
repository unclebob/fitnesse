// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.Result;
import fitnesse.testsystems.slim.results.TestResult;
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

  void substitute(int col, int row, String content);

  int addRow(List<String> list);

  @Deprecated
  void appendContent(int row, String content);

  @Deprecated
  String getCellResult(int col,int row);

  void appendChildTable(int row, Table table);

  @Deprecated
  void setTestStatusOnRow(int row, ExecutionResult testStatus);
  // FoxMe: setTestStatusOnRow becomes updateContent
  void updateContent(int row, TestResult result);

  void updateContent(int col, int row, ExceptionResult exceptionResult);

  @Deprecated
  void setCell(int col, int row, Result response);

  @Deprecated
  void appendContent(int col, int row, Result response);

  public Table asTemplate(CellContentSubstitution substitution) throws SyntaxError;

  interface CellContentSubstitution {
    String substitute(int col, int row, String content) throws SyntaxError;
  }
}
