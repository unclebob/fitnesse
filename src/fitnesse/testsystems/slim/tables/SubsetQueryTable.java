package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

import java.util.List;

public class SubsetQueryTable extends QueryTable {

  public SubsetQueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  protected ExecutionResult scanRowsForMatches(List<Object> queryResultList) {
    QueryResults queryResults = new QueryResults(queryResultList);
    int rows = table.getRowCount();
    for (int tableRow = 2; tableRow < rows; tableRow++) {
      scanRowForMatch(tableRow, queryResults);
    }
    return null;
  }

}
