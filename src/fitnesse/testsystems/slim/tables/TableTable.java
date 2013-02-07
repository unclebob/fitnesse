// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;

import java.util.ArrayList;
import java.util.List;

import static util.ListUtility.list;

public class TableTable extends SlimTable {
  private String doTableId;

  public TableTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  protected String getTableType() {
    return ("tableTable");
  }

  public List<Assertion> getAssertions() {
    Assertion make = constructFixture(getFixtureName());
    Instruction doTable = callFunction(getTableName(), "doTable", tableAsList());
    doTableId = doTable.getId();
    return list(make, makeAssertion(doTable, new TableTableExpectation()));
  }

  public class TableTableExpectation implements Expectation {

    @Override
    public TestResult evaluateExpectation(Object tableReturn) {
      TestResult testResult;
      if (tableReturn == null || "null".equals(tableReturn)) {
        testResult = TestResult.ignore("No results from table");
        table.updateContent(table.getColumnCountInRow(0) - 1, 0, testResult);
      } else if (tableReturn instanceof String) {
        testResult = TestResult.error((String) tableReturn);
        table.updateContent(0, 0, testResult);
      } else {
        TestSummary testSummary = resizeTableAndEvaluateRows(tableReturn);
        getTestContext().increment(testSummary);
        testResult = new TestResult(ExecutionResult.getExecutionResult(testSummary));
      }
      getTestContext().increment(testResult.getExecutionResult());
      return testResult;
    }

    @Override
    public ExceptionResult evaluateException(ExceptionResult exceptionResult) {
      table.updateContent(0, 0, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }
  }

  @SuppressWarnings("unchecked")
  private TestSummary resizeTableAndEvaluateRows(Object returnValue) {
    List<List<Object>> tableResults = (List<List<Object>>) returnValue;
    extendTable(table, tableResults);
    TestSummary testSummary = new TestSummary();
    for (int row = 0; row < tableResults.size(); row++)
      evaluateRow(tableResults, row, testSummary);
    return testSummary;
  }

  private void extendTable(Table table, List<List<Object>> tableResults) {
    addNewRows(table, tableResults);
    extendExistingRows(table, tableResults);
  }

  private void addNewRows(Table table, List<List<Object>> tableResults) {
    while (table.getRowCount() - 1 < tableResults.size()) {
      List<String> l = new ArrayList<String>();
      for (Object s : tableResults.get(table.getRowCount() - 1))
        l.add((String) s);
      table.addRow(l);
    }
  }

  private void extendExistingRows(Table table, List<List<Object>> tableResults) {
    for (int row = 1; row < tableResults.size(); row++)
      extendRow(table, row, tableResults.get(row - 1));
  }

  private void extendRow(Table table, int row, List<Object> cellList) {
    while (table.getColumnCountInRow(row) < cellList.size())
      table.addColumnToRow(row, (String) cellList.get(table.getColumnCountInRow(row)));
  }

  private void evaluateRow(List<List<Object>> tableResults, int resultRow, TestSummary testSummary) {
    final List<Object> rowList = tableResults.get(resultRow);
    for (int col = 0; col < rowList.size(); col++) {
      int tableRow = resultRow + 1;
      String contents = table.getCellContents(col, tableRow);
      String result = (String) rowList.get(col);
      TestResult testResult = getTestResult(result, replaceSymbolsWithFullExpansion(contents));
      if (testResult != null) {
        table.updateContent(col, tableRow, testResult);
        testSummary.add(testResult.getExecutionResult());
      }
    }
  }

  private TestResult getTestResult(String message, String content) {
    TestResult result;
    if (message.equalsIgnoreCase("no change") || message.length() == 0)
      return null; // do nothing
    else if (message.equalsIgnoreCase("pass"))
      result = TestResult.pass(content);
    else if (message.equalsIgnoreCase("fail"))
      result = TestResult.fail(content);
    else if (message.equalsIgnoreCase("ignore"))
      result = TestResult.ignore();
    else if ((result = resultFromMessage(message)) == null)
      result = TestResult.fail(message);
    return result;
  }

  private TestResult resultFromMessage(String contents) {
    int colon = contents.indexOf(":");
    if (colon == -1)
      return null;
    String code = contents.substring(0, colon);
    String message = contents.substring(colon + 1);

    if (code.equalsIgnoreCase("error"))
      return TestResult.error(message);
    else if (code.equalsIgnoreCase("fail"))
      return TestResult.fail(message);
    else if (code.equalsIgnoreCase("pass"))
      return TestResult.pass(message);
    else if (code.equalsIgnoreCase("ignore"))
      return TestResult.ignore(message);
    else if (code.equalsIgnoreCase("report"))
      return TestResult.plain(message);
    else
      return null;
  }
}
