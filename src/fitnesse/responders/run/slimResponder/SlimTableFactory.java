package fitnesse.responders.run.slimResponder;

import fitnesse.slimTables.DecisionTable;
import fitnesse.slimTables.ImportTable;
import fitnesse.slimTables.OrderedQueryTable;
import fitnesse.slimTables.QueryTable;
import fitnesse.slimTables.ScenarioTable;
import fitnesse.slimTables.ScriptTable;
import fitnesse.slimTables.SlimErrorTable;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.SubsetQueryTable;
import fitnesse.slimTables.Table;
import fitnesse.slimTables.TableTable;

public class SlimTableFactory {

  private boolean doesNotHaveColon(String tableType) {
    return tableType.indexOf(":") == -1;
  }

  private boolean beginsWith(String tableType, String typeCode) {
    return tableType.toUpperCase().startsWith(typeCode.toUpperCase());
  }

  public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    String tableType = table.getCellContents(0, 0);
    if (beginsWith(tableType, "dt:") || beginsWith(tableType, "decision:"))
      return new DecisionTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "ordered query:"))
      return new OrderedQueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "subset query:"))
      return new SubsetQueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "query:"))
      return new QueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "table"))
      return new TableTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("script"))
      return new ScriptTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("scenario"))
      return new ScenarioTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("comment"))
      return null;
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId, slimTestContext);
    else if (doesNotHaveColon(tableType))
      return new DecisionTable(table, tableId, slimTestContext);
    else
      return new SlimErrorTable(table, tableId, slimTestContext);
  }
}
