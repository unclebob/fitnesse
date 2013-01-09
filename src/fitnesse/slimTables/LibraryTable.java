package fitnesse.slimTables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.responders.run.slimResponder.SlimTestContext;

public class LibraryTable extends SlimTable {

  private static final String TABLE_TYPE = "library";

  public LibraryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  public List<Object> getInstructions() {
    List<Object> instructions = new ArrayList<Object>();
    for (int row = 1; row < table.getRowCount(); row++) {
      String disgracedClassName = Disgracer.disgraceClassName(table.getCellContents(0, row));
      if (disgracedClassName.length() > 0) {
        instructions.add(constructInstance("library" + row, disgracedClassName, 0, row));
      }
    }
    return instructions;
  }

  @Override
  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {

  }

  @Override
  protected String getTableType() {
    return TABLE_TYPE;
  }

}
