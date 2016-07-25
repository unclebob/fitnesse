// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.List;

import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.SyntaxError;

public interface Table {
  boolean isTearDown();

  String getCellContents(int col, int row);

  int getRowCount();

  int getColumnCountInRow(int row);

  void substitute(int col, int row, String content);

  int addRow(List<String> list);

  void addColumnToRow(int row, String content);

  void appendChildTable(int row, Table table);

  void updateContent(int row, TestResult testResult);

  void updateContent(int col, int row, SlimTestResult testResult);

  void updateContent(int col, int row, SlimExceptionResult exceptionResult);

  Table asTemplate(CellContentSubstitution substitution) throws SyntaxError;

  // Mainly for IDE navigation
  @Override
  String toString();

  interface CellContentSubstitution {
    String substitute(String content) throws SyntaxError;
  }
}
