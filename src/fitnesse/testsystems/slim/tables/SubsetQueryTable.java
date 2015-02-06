package fitnesse.testsystems.slim.tables;

import java.util.List;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class SubsetQueryTable extends QueryTable {

  public SubsetQueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  protected ExecutionResult markRows(QueryResults queryResults, Iterable<MatchedResult> potentialMatchesByScore) {
    List<Integer> unmatchedTableRows = unmatchedRows(table.getRowCount());
    unmatchedTableRows.remove(Integer.valueOf(0));
    unmatchedTableRows.remove(Integer.valueOf(1));
    List<Integer> unmatchedResultRows = unmatchedRows(queryResults.getRows().size());

    while (!isEmpty(potentialMatchesByScore)) {
      MatchedResult bestMatch = takeBestMatch(potentialMatchesByScore);
      markFieldsInMatchedRow(bestMatch.tableRow, bestMatch.resultRow, queryResults);
      unmatchedTableRows.remove(bestMatch.tableRow);
      unmatchedResultRows.remove(bestMatch.resultRow);
    }

    markMissingRows(unmatchedTableRows);
    return null;
  }
}
