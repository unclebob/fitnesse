package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

/**
 * ScriptTable subclass to use in acceptance test to see that DecisionTable based on scenario respects subclasses
 * used in previous script definition.
 */
public class ScriptTableWithVerify extends ScriptTable {
  public ScriptTableWithVerify(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }

  @Override
  protected String getTableType() {
    return "scriptWithVerifyTable";
  }

  protected String getTableKeyword() {
    return "verify script";
  }

  @Override
  protected String getCheckKeyword() {
    return "verify";
  }
}
