package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class LibraryTable extends SlimTable {

  private static final String TABLE_TYPE = "library";

  public LibraryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  public List<SlimAssertion> getAssertions() {
    List<SlimAssertion> instructions = new ArrayList<>();
    for (int row = 1; row < table.getRowCount(); row++) {
      String disgracedClassName = Disgracer.disgraceClassName(table.getCellContents(0, row));
      if (!disgracedClassName.isEmpty()) {
        instructions.add(constructInstance("library" + row, disgracedClassName, 0, row));
      }
    }
    return instructions;
  }

  @Override
  protected String getTableType() {
    return TABLE_TYPE;
  }

}
