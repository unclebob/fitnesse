// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.util.ListUtility;
import fitnesse.responders.run.slimResponder.SlimTestContext;

import java.util.*;

public class QueryTable extends SlimTable {
  private List<String> fieldNames = new ArrayList<String>();
  private String queryId;
  private QueryResults queryResults;

  public QueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  protected String getTableType() {
    return "queryTable";
  }

  public void appendInstructions() {
    if (table.getRowCount() < 2)
      throw new SlimTable.SyntaxError("Query tables must have at least two rows.");
    assignColumns();
    constructFixture();
    queryId = callFunction(getTableName(), "query");
  }

  private void assignColumns() {
    int cols = table.getColumnCountInRow(1);
    for (int col = 0; col < cols; col++)
      fieldNames.add(table.getCellContents(col, 1));
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
    Object queryReturn = returnValues.get(queryId);
    if (queryId == null || queryReturn == null) {
      table.appendToCell(0, 0, error("query method did not return a list."));
      return;
    } else if (queryReturn instanceof String) {
      String message = (String) queryReturn;
      if (isExceptionMessage(message))
        table.appendToCell(0, 0, error(extractExeptionMessage(message)));
      else
        table.appendToCell(0, 0, error(String.format("The query method returned: %s", message)));
    } else {
      scanRowsForMatches(ListUtility.uncheckedCast(Object.class, queryReturn));
    }
  }

  private void scanRowsForMatches(List<Object> queryResultList) throws Exception {
    queryResults = new QueryResults(queryResultList);
    int rows = table.getRowCount();
    for (int tableRow = 2; tableRow < rows; tableRow++)
      scanRowForMatch(tableRow);
    markSurplusRows();
  }

  private void markSurplusRows() throws Exception {
    Set<Integer> unmatchedRows = queryResults.getUnmatchedRows();
    for (int unmatchedRow : unmatchedRows) {
      List<String> surplusRow = queryResults.getList(fieldNames, unmatchedRow);
      int newTableRow = table.addRow(surplusRow);
      failMessage(0, newTableRow, "surplus");
      markMissingFields(surplusRow, newTableRow);
    }
  }

  private void markMissingFields(List<String> surplusRow, int newTableRow) {
    for (int col = 0; col < surplusRow.size(); col++) {
      String surplusField = surplusRow.get(col);
      if (surplusField == null)
        fail(col, newTableRow, "field not present");
    }
  }

  private void scanRowForMatch(int tableRow) throws Exception {
    int matchedRow = queryResults.findBestMatch(tableRow);
    if (matchedRow == -1) {
      replaceAllvariablesInRow(tableRow);
      failMessage(0, tableRow, "missing");
    } else {
      markFieldsInMatchedRow(tableRow, matchedRow);
    }
  }

  private void replaceAllvariablesInRow(int tableRow) {
    int columns = table.getColumnCountInRow(tableRow);
    for (int col = 0; col < columns; col++) {
      String contents = table.getCellContents(col, tableRow);
      table.setCell(col, tableRow, replaceSymbolsWithFullExpansion(contents));
    }
  }

  private void markFieldsInMatchedRow(int tableRow, int matchedRow) {
    int columns = table.getColumnCountInRow(tableRow);
    for (int col = 0; col < columns; col++) {
      markField(tableRow, matchedRow, col);
    }
  }

  private void markField(int tableRow, int matchedRow, int col) {
    String actualValue = queryResults.getCell(fieldNames.get(col), matchedRow);
    String expectedValue = table.getCellContents(col, tableRow);
    table.setCell(col, tableRow, replaceSymbolsWithFullExpansion(expectedValue));
    if (actualValue == null)
      failMessage(col, tableRow, "field not present");
    else if (actualValue.equals(replaceSymbols(expectedValue))) {
      pass(col, tableRow);
    } else {
      expected(col, tableRow, actualValue);
    }
  }

  class QueryResults {
    private List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
    private Set<Integer> unmatchedRows = new HashSet<Integer>();

    public QueryResults(List<Object> queryResultTable) {
      int rowNumber = 0;
      for (Object row : queryResultTable) {
        rows.add(makeRowMap(row));
        unmatchedRows.add(rowNumber++);
      }
    }

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

    public Set<Integer> getUnmatchedRows() {
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

        unmatchedRows.remove(deepestRow);
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
          if (actualValue != null && actualValue.equals(replaceSymbols(expectedValue))) {
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
