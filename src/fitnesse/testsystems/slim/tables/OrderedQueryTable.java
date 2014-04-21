package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;

import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedSet;

public class OrderedQueryTable extends QueryTable {
  private int lastMatchedRow = -1;

  public OrderedQueryTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  @Override
  protected ExecutionResult markRows(QueryResults queryResults, PriorityQueue<MatchedResult> potentialMatchesByScore) {
    int rowCount = table.getRowCount();
    List<Integer> unmatchedResultRows = unmatchedRows(queryResults.getRows().size());

    for (int tableRow = 2; tableRow < rowCount; tableRow++) {
      MatchedResult bestMatch = takeBestMatch(potentialMatchesByScore, tableRow);
      if (bestMatch == null) {
        markMissingRow(tableRow);
      } else {
        markFieldsInMatchedRow(bestMatch.tableRow, bestMatch.resultRow, queryResults);
        lastMatchedRow = bestMatch.resultRow;
        unmatchedResultRows.remove(bestMatch.resultRow);
      }
    }

    return markSurplusRows(queryResults, unmatchedResultRows);
  }

  private MatchedResult takeBestMatch(PriorityQueue<MatchedResult> potentialMatchesByScore, int tableRow) {
    for (MatchedResult bestResult : potentialMatchesByScore) {
      if (bestResult.tableRow == tableRow) {
        removeOtherwiseMatchedResults(potentialMatchesByScore, bestResult);
        return bestResult;
      }
    }
    return null;
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
