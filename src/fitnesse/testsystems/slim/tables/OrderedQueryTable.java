package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;

public class OrderedQueryTable extends QueryTable {
  private int lastMatchedRow = -1;

  public OrderedQueryTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  @Override
  protected void scanRowForMatch(int tableRow, QueryResults queryResults) {
    int matchedRow = queryResults.findBestMatch(tableRow);
    if (matchedRow == -1) {
      replaceAllvariablesInRow(tableRow);
      SlimTestResult testResult = SlimTestResult.fail(null, table.getCellContents(0, tableRow), "missing");
      getTestContext().increment(testResult.getExecutionResult());
      table.updateContent(0, tableRow, testResult);
    } else {
      int columns = table.getColumnCountInRow(tableRow);
      markColumns(tableRow, matchedRow, columns, queryResults);
      lastMatchedRow = matchedRow;
    }
  }

  private void markColumns(int tableRow, int matchedRow, int columns, QueryResults queryResults) {
    for (int col = 0; col < columns; col++) {
      markField(tableRow, matchedRow, col, queryResults);
    }
  }

  @Override
  protected SlimTestResult markMatch(int tableRow, int matchedRow, int col, String message) {
    SlimTestResult testResult;
    if (col == 0 && matchedRow <= lastMatchedRow) {
      testResult = SlimTestResult.fail(null, message, "out of order: row " + (matchedRow + 1));
    } else {
      testResult = SlimTestResult.pass(message);
    }
    return testResult;
  }
}
