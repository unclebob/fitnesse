package fitnesse.testsystems.slim.tables;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class SlimTableFactory {
  private static final Logger LOG = Logger.getLogger(SlimTableFactory.class.getName());
  private static final Map<Class<? extends SlimTable>, Constructor<? extends SlimTable>> CONSTRUCTOR_MAP = new HashMap<Class<? extends SlimTable>, Constructor<? extends SlimTable>>();

  private final Map<String, Class<? extends SlimTable>> tableTypes;
  private final Map<String, String> tableTypeArrays;

  public  SlimTableFactory() {
    tableTypes = new HashMap<String, Class<? extends SlimTable>>(16);
    tableTypeArrays = new HashMap<String, String>();
    addTableType("dt:", DecisionTable.class);
    addTableType("decision:", DecisionTable.class);
    addTableType("ddt:", DynamicDecisionTable.class);
    addTableType("dynamic decision:", DynamicDecisionTable.class);
    addTableType("ordered query:", OrderedQueryTable.class);
    addTableType("subset query:", SubsetQueryTable.class);
    addTableType("query:", QueryTable.class);
    addTableType("table:", TableTable.class);
    addTableType("script", ScriptTable.class);
    addTableType("script:", ScriptTable.class);
    addTableType("scenario", ScenarioTable.class);
    addTableType("import", ImportTable.class);
    addTableType("library", LibraryTable.class);
  }

  protected SlimTableFactory(Map<String, Class<? extends SlimTable>> tableTypes, Map<String, String> tableTypeArrays) {
    this.tableTypes = tableTypes;
    this.tableTypeArrays = tableTypeArrays;
  }

  public void addTableType(String nameOrPrefix, Class<? extends SlimTable> tableClass) {
    if (tableTypes.get(nameOrPrefix) != null) {
      throw new IllegalStateException("A table type named '" + nameOrPrefix + "' already exists");
    }
    tableTypes.put(nameOrPrefix.toLowerCase(), tableClass);
  }

  public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    String tableType = getFullTableName(table.getCellContents(0, 0));

    // First the "exceptions to the rule"
    if (tableType.equalsIgnoreCase("define table type")) {
      parseDefineTableTypeTable(table);
      return null;
    } else if (tableType.equalsIgnoreCase("comment") || tableType.startsWith("comment:")) {
      return null;
    }

    Class<? extends SlimTable> tableClass = getTableType(tableType);

    if (tableClass != null) {
      return newTableForType(tableClass, table, tableId, slimTestContext);
    } else if (!hasColon(tableType)) {
      return new DecisionTable(table, tableId, slimTestContext);
    }

    return new SlimErrorTable(table, tableId, slimTestContext);
  }

  private boolean hasColon(String tableType) {
    return tableType.contains(":");
  }

  public Class<? extends SlimTable> getTableType(String tableType) {
    if (hasColon(tableType)) {
      tableType = tableType.substring(0, tableType.indexOf(':') + 1);
    }
    return tableTypes.get(tableType.toLowerCase());
  }

  private SlimTable newTableForType(Class<? extends SlimTable> tableClass,
                                    Table table, String tableId, SlimTestContext slimTestContext) {
    try {
      return createTable(tableClass, table, tableId, slimTestContext);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Can not create new table instance for class " + tableClass, e);
      return new SlimErrorTable(table, tableId, slimTestContext);
    }
  }

  public static <T extends SlimTable> T createTable(Class<T> tableClass, Table table, String tableId, SlimTestContext slimTestContext) throws Exception {
    Constructor<? extends SlimTable> constructor = CONSTRUCTOR_MAP.get(tableClass);
    if (constructor == null) {
      constructor = tableClass.getConstructor(Table.class, String.class, SlimTestContext.class);
      CONSTRUCTOR_MAP.put(tableClass, constructor);
    }
    return (T) constructor.newInstance(table, tableId, slimTestContext);
  }

  private String getFullTableName(String tableName) {
    if (hasColon(tableName)) {
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

  public SlimTableFactory copy() {
    return new SlimTableFactory(new HashMap<String, Class<? extends SlimTable>>(tableTypes),
            new HashMap<String, String>(tableTypeArrays));
  }
}
