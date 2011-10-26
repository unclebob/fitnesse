package fitnesse.slimTables;

import java.util.HashMap;
import java.util.Map;

import fitnesse.responders.run.slimResponder.SlimTestContext;
import fitnesse.slimTables.SlimTable.Disgracer;

public class SlimTableFactory {

  private boolean doesNotHaveColon(String tableType) {
    return tableType.indexOf(":") == -1;
  }

  private boolean beginsWith(String tableType, String typeCode) {
    return tableType.toUpperCase().startsWith(typeCode.toUpperCase());
  }

  private Map<String, String> tableTypeArrays = new HashMap<String, String>();
  
  public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    String tableType = getFullTableName(table.getCellContents(0, 0));
    if (beginsWith(tableType, "dt:") || beginsWith(tableType, "decision:"))
      return new DecisionTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "ordered query:"))
      return new OrderedQueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "subset query:"))
      return new SubsetQueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "query:"))
      return new QueryTable(table, tableId, slimTestContext);
    else if (beginsWith(tableType, "table:"))
      return new TableTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("script"))
      return new ScriptTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("scenario"))
      return new ScenarioTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("comment"))
      return null;
    else if (tableType.equalsIgnoreCase("import"))
      return new ImportTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("library"))
      return new LibraryTable(table, tableId, slimTestContext);
    else if (tableType.equalsIgnoreCase("define table type"))  {
        parseDefineTableTypeTable(table);
        return null;
      }
    else if (doesNotHaveColon(tableType))
      return new DecisionTable(table, tableId, slimTestContext);
      
    return new SlimErrorTable(table, tableId, slimTestContext);
  }

  private String getFullTableName(String tableName) {
    if (tableName.contains(":")) {
      return tableName;
    }
    
    //check for a table type defined in a table type definition
    String disgracedName = Disgracer.disgraceClassName(tableName);
    if (tableTypeArrays.containsKey(disgracedName)) {
      return tableTypeArrays.get(disgracedName) + ":" + tableName;
    }
    return tableName;
  }

  private SlimTable parseDefineTableTypeTable(Table table) {
    for (int rowIndex = 1; rowIndex < table.getRowCount(); rowIndex++)
      parseDefineTableTypeRow(table, rowIndex);
    return null;
  }

  private void parseDefineTableTypeRow(Table table, int rowIndex) {
    if (table.getColumnCountInRow(rowIndex) >= 2) {
      String fixtureName = table.getCellContents(0, rowIndex);
      String fixture = Disgracer.disgraceClassName(fixtureName);
      String tableSpecifier = table.getCellContents(1, rowIndex).toLowerCase();
      tableTypeArrays.put(fixture, makeTableType(tableSpecifier));
    }
  }

  private String makeTableType(String tableSpecifier) {
    String tableType = tableSpecifier.replace(':', ' ');
    if (tableType.startsWith("as"))
      tableType = tableType.substring(2);

    return tableType.trim();
  }
}
