// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.SyntaxError;

import java.util.List;

public interface Table {
  String getCellContents(int col, int row);
  // FixMe: -AJM- Can I remove this without functional change???
  String getUnescapedCellContents(int col, int row);

  int getRowCount();

  int getColumnCountInRow(int row);

  void substitute(int col, int row, String content);

  int addRow(List<String> list);

  void addColumnToRow(int row, String content);

  void appendChildTable(int row, Table table);

  void updateContent(int row, TestResult testResult);

  void updateContent(int col, int row, TestResult testResult);

  void updateContent(int col, int row, ExceptionResult exceptionResult);

  public Table asTemplate(CellContentSubstitution substitution) throws SyntaxError;

  // Mainly for IDE navigation
  public String toString();

  interface CellContentSubstitution {
    String substitute(int col, int row, String content) throws SyntaxError;
  }
}
