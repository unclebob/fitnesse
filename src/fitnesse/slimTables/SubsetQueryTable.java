package fitnesse.slimTables;

import java.util.List;

import fitnesse.responders.run.slimResponder.SlimTestContext;

public class SubsetQueryTable extends QueryTable {

  public SubsetQueryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  protected void scanRowsForMatches(List<Object> queryResultList) throws Exception {
    queryResults = new QueryResults(queryResultList);
    int rows = table.getRowCount();
    for (int tableRow = 2; tableRow < rows; tableRow++)
      scanRowForMatch(tableRow);
  }

}
