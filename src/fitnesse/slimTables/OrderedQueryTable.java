package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.SlimTestContext;

public class OrderedQueryTable extends QueryTable {
  private int lastMatchedRow = -1;

  public OrderedQueryTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  @Override
  protected void scanRowForMatch(int tableRow) throws Exception {
    int matchedRow = queryResults.findBestMatch(tableRow);
    if (matchedRow == -1) {
      replaceAllvariablesInRow(tableRow);
      failMessage(0, tableRow, "missing");
    } else {
      int columns = table.getColumnCountInRow(tableRow);
      markColumns(tableRow, matchedRow, columns);
      lastMatchedRow = matchedRow;
    }
  }

  private void markColumns(int tableRow, int matchedRow, int columns) {
    for (int col = 0; col < columns; col++) {
      markField(tableRow, matchedRow, col);
    }
  }

  protected void markMatch(int tableRow, int matchedRow, int col) {
    if (col == 0 && matchedRow <= lastMatchedRow) {
      failMessage(0, tableRow, "out of order: row " + (matchedRow+1));
    } else {
      pass(col, tableRow);
    }
  }
}
