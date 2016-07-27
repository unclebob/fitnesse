package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class DynamicDecisionTable extends DecisionTable {
  private static final String TABLE_TYPE = "dynamicDecisionTable";

  public DynamicDecisionTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
    this.setterMethodExtractor = new MethodExtractor();
    this.setterMethodExtractor.add(new MethodExtractorRule(".+", "set", "$0"));
    this.getterMethodExtractor = new MethodExtractor();
    this.getterMethodExtractor.add(new MethodExtractorRule(".+", "get", "$0"));
  }

  @Override
  protected String getTableType() {
    return TABLE_TYPE;
  }

}
