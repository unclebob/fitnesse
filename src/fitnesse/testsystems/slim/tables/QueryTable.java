// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;

import static util.ListUtility.list;

public class QueryTable extends SlimTable {
  protected List<String> fieldNames = new ArrayList<String>();
  private String queryId;

  public QueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  protected String getTableType() {
    return "queryTable";
  }

  public boolean matches(String actual, String expected) {
    if (actual == null || expected == null)
      return false;
    if (actual.equals(replaceSymbols(expected)))
      return true;
    Comparator c = new Comparator(actual, expected);
    return c.matches();
  }

  public SlimTestResult matchMessage(String actual, String expected) {
    if (actual == null)
      return SlimTestResult.fail("NULL");
    if (actual.equals(replaceSymbols(expected)))
      return SlimTestResult.pass(replaceSymbolsWithFullExpansion(expected));
    Comparator c = new Comparator(actual, expected);
    return c.evaluate();
  }

  @Override
  public List<SlimAssertion> getAssertions() throws SyntaxError {
    if (table.getRowCount() < 2)
      throw new SyntaxError("Query tables must have at least two rows.");
    assignColumns();
    SlimAssertion make = constructFixture(getFixtureName());
    SlimAssertion ti = makeAssertion(callFunction(getTableName(), "table", tableAsList()),
            new SilentReturnExpectation(0, 0));
    SlimAssertion qi = makeAssertion(callFunction(getTableName(), "query"),
            new QueryTableExpectation());
    String tableInstruction = ti.getInstruction().getId();
    queryId = qi.getInstruction().getId();
    return list(make, ti, qi);
  }

  private void assignColumns() {
    int cols = table.getColumnCountInRow(1);
    for (int col = 0; col < cols; col++)
      fieldNames.add(table.getCellContents(col, 1));
  }

  public class QueryTableExpectation implements SlimExpectation {

    @Override
    public TestResult evaluateExpectation(Object queryReturn) {
      SlimTestResult testResult;
      if (queryId == null || queryReturn == null) {
        testResult = SlimTestResult.error("query method did not return a list");
        table.updateContent(0, 0, testResult);
      } else if (queryReturn instanceof List) {
        testResult = new SlimTestResult(scanRowsForMatches((List<Object>) queryReturn));
      } else {
        testResult = SlimTestResult.error(String.format("The query method returned: %s", queryReturn));
        table.updateContent(0, 0, testResult);
      }
      return testResult;
    }

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      table.updateContent(0, 0, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }
  }


  protected ExecutionResult scanRowsForMatches(List<Object> queryResultList) {
    final QueryResults queryResults = new QueryResults(queryResultList);
    int rows = table.getRowCount();
    for (int tableRow = 2; tableRow < rows; tableRow++)
      scanRowForMatch(tableRow, queryResults);
    return markSurplusRows(queryResults);
  }

  private ExecutionResult markSurplusRows(final QueryResults queryResults) {
    List<Integer> unmatchedRows = queryResults.getUnmatchedRows();
    ExecutionResult result = ExecutionResult.PASS;
    for (int unmatchedRow : unmatchedRows) {
      List<String> surplusRow = queryResults.getList(fieldNames, unmatchedRow);
      int newTableRow = table.addRow(surplusRow);
      SlimTestResult testResult = SlimTestResult.fail(surplusRow.get(0), null, "surplus");
      table.updateContent(0, newTableRow, testResult);
      getTestContext().increment(ExecutionResult.FAIL);
      markMissingFields(surplusRow, newTableRow);
      result = ExecutionResult.FAIL;
    }
    return result;
  }

  private void markMissingFields(List<String> surplusRow, int newTableRow) {
    for (int col = 0; col < surplusRow.size(); col++) {
      String surplusField = surplusRow.get(col);
      if (surplusField == null) {
        String fieldName = fieldNames.get(col);
        SlimTestResult testResult = SlimTestResult.fail(String.format("field %s not present", fieldName));
        table.updateContent(col, newTableRow, testResult);
        getTestContext().increment(testResult.getExecutionResult());
      }
    }
  }

  protected void scanRowForMatch(int tableRow, QueryResults queryResults) {
    int matchedRow = queryResults.findBestMatch(tableRow);
    if (matchedRow == -1) {
      replaceAllvariablesInRow(tableRow);
      SlimTestResult testResult = SlimTestResult.fail(null, table.getCellContents(0, tableRow), "missing");
      table.updateContent(0, tableRow, testResult);
      getTestContext().increment(testResult.getExecutionResult());
    } else {
      markFieldsInMatchedRow(tableRow, matchedRow, queryResults);
    }
  }

  protected void replaceAllvariablesInRow(int tableRow) {
    int columns = table.getColumnCountInRow(tableRow);
    for (int col = 0; col < columns; col++) {
      String contents = table.getCellContents(col, tableRow);
      table.substitute(col, tableRow, replaceSymbolsWithFullExpansion(contents));
    }
  }

  protected void markFieldsInMatchedRow(int tableRow, int matchedRow, QueryResults queryResults) {
    int columns = table.getColumnCountInRow(tableRow);
    for (int col = 0; col < columns; col++) {
      markField(tableRow, matchedRow, col, queryResults);
    }
  }

  protected TestResult markField(int tableRow, int matchedRow, int col, QueryResults queryResults) {
    if (col >= fieldNames.size())
      return null; // ignore strange table geometry.
    String fieldName = fieldNames.get(col);
    String actualValue = queryResults.getCell(fieldName, matchedRow);
    String expectedValue = table.getCellContents(col, tableRow);
    SlimTestResult testResult;
    if (actualValue == null)
      testResult = SlimTestResult.fail(String.format("field %s not present", fieldName), expectedValue);
    else if (expectedValue == null || expectedValue.length() == 0)
      testResult = SlimTestResult.ignore(actualValue);
    else {
      testResult = matchMessage(actualValue, expectedValue);
      if (testResult == null)
        testResult = SlimTestResult.fail(actualValue, replaceSymbolsWithFullExpansion(expectedValue));
      else if (testResult.getExecutionResult() == ExecutionResult.PASS)
        testResult = markMatch(tableRow, matchedRow, col, testResult.getMessage());
    }
    table.updateContent(col, tableRow, testResult);
    getTestContext().increment(testResult.getExecutionResult());
    return testResult;
  }

  protected SlimTestResult markMatch(int tableRow, int matchedRow, int col, String message) {
    return SlimTestResult.pass(message);
  }

  class QueryResults {
    private List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
    private List<Integer> unmatchedRows = new ArrayList<Integer>();

    public QueryResults(List<Object> queryResultTable) {
      int rowNumber = 0;
      for (Object row : queryResultTable) {
        rows.add(makeRowMap(row));
        unmatchedRows.add(rowNumber++);
      }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> makeRowMap(Object row) {
      Map<String, String> rowMap = new HashMap<String, String>();
      for (List<Object> columnPair : (List<List<Object>>) row) {
        String fieldName = (String) columnPair.get(0);
        String fieldValue = (String) columnPair.get(1);
        rowMap.put(fieldName, fieldValue);
      }
      return rowMap;
    }

    public int findBestMatch(int tableRow) {
      return new QueryMatcher().findBestMatch(tableRow);
    }

    public List<String> getList(List<String> fieldNames, int row) {
      List<String> result = new ArrayList<String>();
      for (String name : fieldNames)
        result.add(rows.get(row).get(name));

      return result;
    }

    public String getCell(String name, int row) {
      return rows.get(row).get(name);
    }

    public List<Integer> getUnmatchedRows() {
      return unmatchedRows;
    }

    private class QueryMatcher {
      private int matchDepth;
      private int deepestRow;
      private List<Integer> matchCandidates;

      private QueryMatcher() {
        matchDepth = -1;
        deepestRow = -1;
        matchCandidates = new ArrayList<Integer>(unmatchedRows);
      }

      public int findBestMatch(int tableRow) {
        for (int fieldIndex = 0; fieldIndex < fieldNames.size(); fieldIndex++)
          new FieldMatcher(fieldIndex, tableRow).eliminateRowsThatDontMatchField();

        if (deepestRow >= 0)
          unmatchedRows.remove(unmatchedRows.indexOf(deepestRow));
        return deepestRow;
      }

      class FieldMatcher {
        private int fieldIndex;
        private int tableRow;

        FieldMatcher(int fieldIndex, int tableRow) {
          this.fieldIndex = fieldIndex;
          this.tableRow = tableRow;
        }

        private void eliminateRowsThatDontMatchField() {
          String fieldName = fieldNames.get(fieldIndex);
          Iterator<Integer> rowIterator = matchCandidates.iterator();
          while (rowIterator.hasNext())
            eliminateUnmatchingRow(rowIterator, fieldName);
        }

        private void eliminateUnmatchingRow(Iterator<Integer> rowIterator, String fieldName) {
          int row = rowIterator.next();
          String actualValue = rows.get(row).get(fieldName);
          String expectedValue = table.getCellContents(fieldIndex, tableRow);
          if (matches(actualValue, expectedValue)) {
            recordMatch(row);
          } else {
            rowIterator.remove();
          }
        }

        private void recordMatch(int row) {
          if (matchDepth < fieldIndex) {
            matchDepth = fieldIndex;
            deepestRow = row;
          }
        }
      }
    }
  }
}
