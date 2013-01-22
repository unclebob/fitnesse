package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

import java.util.ArrayList;
import java.util.List;

public class LibraryTable extends SlimTable {

  private static final String TABLE_TYPE = "library";

  public LibraryTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  public List<Assertion> getAssertions() {
    List<Assertion> instructions = new ArrayList<Assertion>();
    for (int row = 1; row < table.getRowCount(); row++) {
      String disgracedClassName = Disgracer.disgraceClassName(table.getCellContents(0, row));
      if (disgracedClassName.length() > 0) {
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
