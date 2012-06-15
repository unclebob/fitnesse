package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.SlimTestContext;
import org.junit.Before;

import java.util.Map;

public class AlternateSlimTableFactoryTest extends SlimTableFactoryTest {

  @Before
  public void moreSetUp() {
    slimTableFactory = SlimTableFactory.getInstance(AlternateSlimTableFactory.class.getName());
    map.put("alt:", AltTable.class);
  }

  public static class AlternateSlimTableFactory extends SlimTableFactory {
    @Override
    public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
      String tableType = table.getCellContents(0, 0);
      if (beginsWith(tableType, "alt:"))
        return new AltTable(table, tableId, slimTestContext);
      else
        return super.makeSlimTable(table, tableId, slimTestContext);
    }
  }

  private static class AltTable extends SlimTable {
    public AltTable(Table table, String id, SlimTestContext testContext) {
      super(table, id, testContext);
    }

    @Override
    protected String getTableType() {
      return "alt";
    }

    @Override
    public void appendInstructions() {
      throw new UnsupportedOperationException("This operation is not yet supported");
    }

    @Override
    public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
      throw new UnsupportedOperationException("This operation is not yet supported");
    }
  }
}