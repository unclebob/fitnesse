// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;

public class TableTable extends SlimTable {

  public TableTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  @Override
  protected String getTableType() {
    return ("tableTable");
  }

  @Override
  public List<SlimAssertion> getAssertions() {
    SlimAssertion make = constructFixture(getFixtureName());
    Instruction doTable = callFunction(getTableName(), "doTable", tableAsList());
    return Arrays.asList(make, makeAssertion(doTable, new TableTableExpectation()));
  }

  public class TableTableExpectation implements SlimExpectation {

    @Override
    public TestResult evaluateExpectation(Object tableReturn) {
      SlimTestResult testResult;
      if (tableReturn == null || "null".equals(tableReturn)) {
        testResult = SlimTestResult.ignore("No results from table");
        table.updateContent(table.getColumnCountInRow(0) - 1, 0, testResult);
      } else if (tableReturn instanceof String) {
        testResult = SlimTestResult.error((String) tableReturn);
        table.updateContent(0, 0, testResult);
      } else {
        TestSummary testSummary = resizeTableAndEvaluateRows(tableReturn);
        getTestContext().increment(testSummary);
        testResult = new SlimTestResult(ExecutionResult.getExecutionResult(testSummary));
        testResult.setVariables(getSymbolsToStore());
      }
      getTestContext().increment(testResult.getExecutionResult());
      return testResult;
    }

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
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
      List<String> l = new ArrayList<>();
      for (Object s : tableResults.get(table.getRowCount() - 1))
        l.add((String) s);
      table.addRow(l);
    }
  }

  private void extendExistingRows(Table table, List<List<Object>> tableResults) {
    for (int row = 1; row < table.getRowCount(); row++)
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
      SlimTestResult testResult = getTestResult(result, replaceSymbolsWithFullExpansion(contents));
      table.updateContent(col, tableRow, testResult);
      testSummary.add(testResult.getExecutionResult());
    }
  }

  private SlimTestResult getTestResult(String message, String content) {
    SlimTestResult result;
    if (message.equalsIgnoreCase("no change") || message.isEmpty())
      result = SlimTestResult.plain(content);
    else if (message.equalsIgnoreCase("pass"))
      result = SlimTestResult.pass(content);
    else if (message.equalsIgnoreCase("fail"))
      result = SlimTestResult.fail(content);
    else if (message.equalsIgnoreCase("ignore"))
      result = SlimTestResult.ignore(content);
    else
      result = resultFromMessage(message, content);
    return result;
  }

  private SlimTestResult resultFromMessage(String codeAndMessage, String content) {
    int colon = codeAndMessage.indexOf(":");
    if (colon == -1)
      return SlimTestResult.fail(manageSymbolInContent(content, codeAndMessage));
    String code = codeAndMessage.substring(0, colon);
    String message = codeAndMessage.substring(colon + 1);

    message = manageSymbolInContent(content, message);

    if (code.equalsIgnoreCase("error"))
      return SlimTestResult.error(message);
    else if (code.equalsIgnoreCase("fail"))
      return SlimTestResult.fail(message);
    else if (code.equalsIgnoreCase("pass"))
      return SlimTestResult.pass(message);
    else if (code.equalsIgnoreCase("ignore"))
      return SlimTestResult.ignore(message);
    else if (code.equalsIgnoreCase("report"))
      return SlimTestResult.plain(message);
    else //not managed code
      return SlimTestResult.fail(manageSymbolInContent(content, codeAndMessage));
  }

  private String manageSymbolInContent(String content, String message) {
    String symbolName = isSymbolAssignment(content);
    if (symbolName != null) {
      setSymbol(symbolName, message, true);
      message = String.format("$%s<-[%s]", symbolName, message);
    }
    return message;
  }
}
