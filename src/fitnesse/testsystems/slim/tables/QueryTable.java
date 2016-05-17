// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.*;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.util.StringUtils;

import static fitnesse.slim.SlimSymbol.isSymbolAssignment;

public class QueryTable extends SlimTable {
  private static final String COMMENT_COLUMN_MARKER = "#";
  protected List<String> fieldNames = new ArrayList<>();

  public QueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
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
    return Arrays.asList(make, ti, qi);
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
      if (queryReturn == null) {
        testResult = SlimTestResult.testNotRun();
      } else if (queryReturn instanceof List) {
        testResult = new SlimTestResult(scanRowsForMatches((List<List<List<Object>>>) queryReturn));
        testResult.setVariables(getSymbolsToStore());
      } else {
        testResult = SlimTestResult.error(String.format("The query method returned: %s", queryReturn));
        table.updateContent(0, 0, testResult);
        getTestContext().increment(testResult.getExecutionResult());
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

  private ExecutionResult scanRowsForMatches(List<List<List<Object>>> queryResultList) {
    final QueryResults queryResults = new QueryResults(queryResultList);

    Collection<MatchedResult> potentialMatches = queryResults.scorePotentialMatches();

    List<MatchedResult> potentialMatchesByScore = new ArrayList<>(potentialMatches);
    Collections.sort(potentialMatchesByScore, MatchedResult.compareByScore());

    return markRows(queryResults, potentialMatchesByScore);
  }

  protected ExecutionResult markRows(QueryResults queryResults, Iterable<MatchedResult> potentialMatchesByScore) {
    List<Integer> unmatchedTableRows = unmatchedRows(table.getRowCount());
    unmatchedTableRows.remove(Integer.valueOf(0));
    unmatchedTableRows.remove(Integer.valueOf(1));
    List<Integer> unmatchedResultRows = unmatchedRows(queryResults.getRows().size());

    markMatchedRows(queryResults, potentialMatchesByScore, unmatchedTableRows, unmatchedResultRows);
    markMissingRows(unmatchedTableRows);
    markSurplusRows(queryResults, unmatchedResultRows);

    return !unmatchedTableRows.isEmpty() || !unmatchedResultRows.isEmpty() ? ExecutionResult.FAIL : ExecutionResult.PASS;
  }

  protected void markMatchedRows(QueryResults queryResults, Iterable<MatchedResult> potentialMatchesByScore, List<Integer> unmatchedTableRows, List<Integer> unmatchedResultRows) {
    while (!isEmpty(potentialMatchesByScore)) {
      MatchedResult bestMatch = takeBestMatch(potentialMatchesByScore);
      markFieldsInMatchedRow(bestMatch.tableRow, bestMatch.resultRow, queryResults);
      unmatchedTableRows.remove(bestMatch.tableRow);
      unmatchedResultRows.remove(bestMatch.resultRow);
    }
  }

  protected MatchedResult takeBestMatch(Iterable<MatchedResult> potentialMatchesByScore) {
    MatchedResult bestResult = potentialMatchesByScore.iterator().next();

    removeOtherwiseMatchedResults(potentialMatchesByScore, bestResult);

    return bestResult;
  }

  protected boolean isEmpty(Iterable<MatchedResult> iterable) {
    return !iterable.iterator().hasNext();
  }

  protected void removeOtherwiseMatchedResults(Iterable<MatchedResult> potentialMatchesByScore, MatchedResult bestResult) {
    Iterator<MatchedResult> iterator = potentialMatchesByScore.iterator();

    while (iterator.hasNext()) {
      MatchedResult otherResult = iterator.next();
      if (otherResult.tableRow.equals(bestResult.tableRow) || otherResult.resultRow.equals(bestResult.resultRow))
        iterator.remove();
    }
  }

  protected List<Integer> unmatchedRows(int rowCount) {
    List<Integer> result = new ArrayList<>(rowCount);

    for (int i = 0; i < rowCount; i++) {
      result.add(i);
    }

    return result;
  }

  protected void markMissingRows(List<Integer> missingRows) {
    for (int missingRow : missingRows) {
      markMissingRow(missingRow);
    }
  }

  protected void markMissingRow(int missingRow) {
    replaceAllvariablesInRow(missingRow);
    SlimTestResult testResult = SlimTestResult.fail(null, table.getCellContents(0, missingRow), "missing");
    table.updateContent(0, missingRow, testResult);
    getTestContext().increment(testResult.getExecutionResult());
  }

  protected void markSurplusRows(final QueryResults queryResults, List<Integer> unmatchedRows) {
    for (int unmatchedRow : unmatchedRows) {
      List<String> surplusRow = queryResults.getList(fieldNames, unmatchedRow);
      int newTableRow = table.addRow(surplusRow);
      SlimTestResult testResult = SlimTestResult.fail(surplusRow.get(0), null, "surplus");
      table.updateContent(0, newTableRow, testResult);
      getTestContext().increment(ExecutionResult.FAIL);
      markMissingFields(surplusRow, newTableRow);
    }
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
    if (fieldName.startsWith(COMMENT_COLUMN_MARKER))
      testResult = SlimTestResult.plain();
    else if (actualValue == null)
      testResult = SlimTestResult.fail(String.format("field %s not present", fieldName), expectedValue);
    else if (expectedValue == null || expectedValue.isEmpty())
      testResult = SlimTestResult.ignore(actualValue);
    else {
      String symbolName = ifSymbolAssignment(expectedValue);
      if (symbolName != null) {
        setSymbol(symbolName, actualValue, true);
        testResult = SlimTestResult.ignore(String.format("$%s<-[%s]", symbolName, actualValue));
      } else {
        testResult = matchMessage(actualValue, expectedValue);
        if (testResult == null)
          testResult = SlimTestResult.fail(actualValue, replaceSymbolsWithFullExpansion(expectedValue));
        else if (testResult.getExecutionResult() == ExecutionResult.PASS)
          testResult = markMatch(tableRow, matchedRow, col, testResult.getMessage());
      }
    }
    table.updateContent(col, tableRow, testResult);
    getTestContext().increment(testResult.getExecutionResult());
    return testResult;
  }

  protected SlimTestResult markMatch(int tableRow, int matchedRow, int col, String message) {
    return SlimTestResult.pass(message);
  }

  protected class QueryResults {
    private List<QueryResultRow> rows = new ArrayList<>();

    public QueryResults(List<List<List<Object>>> queryResultTable) {
      for (int i = 0; i < queryResultTable.size(); i++) {
        rows.add(new QueryResultRow(i, queryResultTable.get(i)));
      }

      rows = Collections.unmodifiableList(rows);
    }

    public Collection<MatchedResult> scorePotentialMatches() {
      Collection<MatchedResult> result = new ArrayList<>();

      int rows = table.getRowCount();
      for (int tableRow = 2; tableRow < rows; tableRow++)
        result.addAll(new QueryMatcher(fieldNames).scoreMatches(tableRow));

      return result;
    }

    public List<String> getList(List<String> fieldNames, int row) {
      List<String> result = new ArrayList<>();
      for (String name : fieldNames)
        result.add(rows.get(row).get(name));

      return result;
    }

    public String getCell(String name, int row) {
      return rows.get(row).get(name);
    }

    public List<QueryResultRow> getRows() {
      return rows;
    }

    private class QueryMatcher {
      private final List<String> fields;

      private QueryMatcher(List<String> fields) {
        this.fields = fields;
      }

      public Collection<MatchedResult> scoreMatches(int tableRow) {
        Collection<MatchedResult> result = new ArrayList<>();

        for (QueryResultRow row : rows) {
          MatchedResult match = scoreMatch(table, tableRow, row);
          if (match.score > 0)
            result.add(match);
        }

        return result;
      }

      private MatchedResult scoreMatch(Table table, int tableRow, QueryResultRow row) {
        int score = 0;

        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
          String fieldName = fields.get(fieldIndex);

          if (!fieldName.startsWith(COMMENT_COLUMN_MARKER)) {
            String actualValue = row.get(fieldName);
            String expectedValue = table.getCellContents(fieldIndex, tableRow);

            if(isSymbolAssignment(expectedValue) != null) {
              continue;
            }

            if (matches(actualValue, expectedValue)) {
              score++;
            } else if (!StringUtils.isBlank(expectedValue)) {
              break;
            }
          }
        }

        return new MatchedResult(tableRow, row.index, score);
      }
    }

    private class QueryResultRow {
      private final int index;
      private final Map<String, String> values;

      public QueryResultRow(int index, List<List<Object>> values) {
        this.index = index;
        Map<String, String> rowMap = new HashMap<>();
        for (List<Object> columnPair : values) {
          String fieldName = (String) columnPair.get(0);
          String fieldValue = (String) columnPair.get(1);
          rowMap.put(fieldName, fieldValue);
        }
        this.values = rowMap;
      }

      public String get(String fieldName) {
        return values.get(fieldName);
      }
    }
  }

  protected static class MatchedResult {
    final Integer tableRow;
    final Integer resultRow;
    final int score;

    public MatchedResult(int tableRow, int resultRow, int score) {
      this.tableRow = tableRow;
      this.resultRow = resultRow;
      this.score = score;
    }

    public static java.util.Comparator<MatchedResult> compareByScore() {
      return new java.util.Comparator<MatchedResult>() {
        @Override
        public int compare(MatchedResult o1, MatchedResult o2) {
          return o2.score - o1.score;
        }
      };
    }
  }
}
