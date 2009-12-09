package fitnesse.slimTables;

import java.util.Map;

import fitnesse.responders.run.slimResponder.SlimTestContext;

public class LibraryTable extends SlimTable {

  private static final String TABLE_TYPE = "library";

  public LibraryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  public void appendInstructions() {
    for (int row = 1; row < table.getRowCount(); row++) {
      String disgracedClassName = Disgracer.disgraceClassName(table.getCellContents(0, row));
      if (disgracedClassName.length() > 0) {
        constructInstance("library" + row, disgracedClassName, 0, row);
      }
    }
  }

  @Override
  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {

  }

  @Override
  protected String getTableType() {
    return TABLE_TYPE;
  }

}
