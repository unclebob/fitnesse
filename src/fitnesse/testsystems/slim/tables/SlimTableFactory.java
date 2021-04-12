package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlimTableFactory {
  private static final Logger LOG = Logger.getLogger(SlimTableFactory.class.getName());
  private static final Map<Class<? extends SlimTable>, Constructor<? extends SlimTable>> CONSTRUCTOR_MAP = new HashMap<>();

  private final Map<String, Class<? extends SlimTable>> tableTypes;
  private final Map<String, String> tableTypeArrays;
  private final Map<String, String> aliasArrays;

  public  SlimTableFactory() {
    tableTypes = new HashMap<>(16);
    tableTypeArrays = new HashMap<>();
    aliasArrays = new HashMap<>();
    addTableType("dt", DecisionTable.class);
    addTableType("decision", DecisionTable.class);
    addTableType("ddt", DynamicDecisionTable.class);
    addTableType("dynamic decision", DynamicDecisionTable.class);
    addTableType("ordered query", OrderedQueryTable.class);
    addTableType("subset query", SubsetQueryTable.class);
    addTableType("query", QueryTable.class);
    addTableType("table", TableTable.class);
    addTableType("script", ScriptTable.class);
    addTableType("script:", ScriptTable.class);
    addTableType("verify script", ScriptTableWithVerify.class);
    addTableType("scenario", ScenarioTable.class);
    addTableType("import", ImportTable.class);
    addTableType("library", LibraryTable.class);
    addTableType("baseline", BaselineDecisionTable.class);
  }

  protected SlimTableFactory(Map<String, Class<? extends SlimTable>> tableTypes, Map<String, String> tableTypeArrays, Map<String, String> aliasArrays) {
    this.tableTypes = tableTypes;
    this.tableTypeArrays = tableTypeArrays;
    this.aliasArrays = aliasArrays;

  }

  public void addTableType(String nameOrPrefix, Class<? extends SlimTable> tableClass) {
    if (tableTypes.get(nameOrPrefix) != null) {
      throw new IllegalStateException("A table type named '" + nameOrPrefix + "' already exists");
    }
    tableTypes.put(StringUtils.replace(nameOrPrefix.toLowerCase(), ":", ""), tableClass);
  }

  public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    SlimTable newTable;
    TableTypeAndName nameAndType = getTableNameAndType(table.getCellContents(0, 0));

    // First the "exceptions to the rule"
    if (nameAndType.hasTableName("define alias")) {
      parseDefineAliasTable(table);
      return null;
    } else if (nameAndType.hasTableName("define table type")) {
      parseDefineTableTypeTable(table);
      return null;
    } else if (nameAndType.hasTableType("comment") || nameAndType.hasTableName("comment")) {
      return null;
    }

    Class<? extends SlimTable> tableClass = getTableType(nameAndType.tableType);

    if (tableClass != null) {
      newTable = newTableForType(tableClass, table, tableId, slimTestContext);
    } else if (nameAndType.mayBeDecisionTable()) {
      newTable = new DecisionTable(table, tableId, slimTestContext);
    } else {
    	newTable = new SlimErrorTable(table, tableId, slimTestContext);
    }
    newTable.setFixtureName(nameAndType.tableName);
    return newTable;
  }

  private boolean hasColon(String tableType) {
    return tableType.contains(":");
  }

  public Class<? extends SlimTable> getTableType(String tableType) {
    return tableTypes.get(tableType.toLowerCase().trim());
  }

  private SlimTable newTableForType(Class<? extends SlimTable> tableClass,
                                    Table table, String tableId, SlimTestContext slimTestContext) {
    try {
      return createTable(tableClass, table, tableId, slimTestContext);
    } catch (TableCreationException e) {
      LOG.log(Level.WARNING, e.getMessage(), e);
      return new SlimErrorTable(table, tableId, slimTestContext);
    }
  }

  public static <T extends SlimTable> T createTable(Class<T> tableClass, Table table, String tableId, SlimTestContext slimTestContext) throws TableCreationException {
    Constructor<? extends SlimTable> constructor = CONSTRUCTOR_MAP.get(tableClass);
    try {
      if (constructor == null) {
        constructor = tableClass.getConstructor(Table.class, String.class, SlimTestContext.class);
        CONSTRUCTOR_MAP.put(tableClass, constructor);
      }
      return (T) constructor.newInstance(table, tableId, slimTestContext);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new TableCreationException("Can not create new table instance for class " + tableClass.getName(), e);
    }
  }

  private TableTypeAndName getTableNameAndType(String tableName) {
    TableTypeAndName nameAndType;
    if (hasColon(tableName)) {
      nameAndType = new TableTypeAndName(tableName.split(":"));
    } else if (getTableType(tableName) != null) {
      // First column only contains table type, such as "| scenario |"
      nameAndType = new TableTypeAndName(tableName, "");
    } else {
      nameAndType = new TableTypeAndName("", tableName);
    }

    String disgracedName = Disgracer.disgraceClassName(nameAndType.tableName);
    if (aliasArrays.containsKey(disgracedName)) {
      String aliasedTableName = aliasArrays.get(disgracedName);
      if (hasColon(aliasedTableName)) {
        String[] aliasParts = aliasedTableName.split(":");
        return new TableTypeAndName(
          aliasParts[0].isEmpty() ? nameAndType.tableType : aliasParts[0],
          (aliasParts.length <= 1 || aliasParts[1].isEmpty()) ? nameAndType.tableName : aliasParts[1]);
      }
      return new TableTypeAndName(nameAndType.tableType, aliasedTableName);
    } else if (StringUtils.isBlank(nameAndType.tableType) && tableTypeArrays.containsKey(disgracedName)) {
      return new TableTypeAndName(tableTypeArrays.get(disgracedName), nameAndType.tableName);
    }
    return nameAndType;
  }

  private void parseDefineTableTypeTable(Table table) {
    for (int rowIndex = 1; rowIndex < table.getRowCount(); rowIndex++)
      parseDefineTableTypeRow(table, rowIndex);
  }

  private void parseDefineTableTypeRow(Table table, int rowIndex) {
    if (table.getColumnCountInRow(rowIndex) >= 2) {
      String fixtureName = table.getCellContents(0, rowIndex);
      String fixture = Disgracer.disgraceClassName(fixtureName);
      String tableSpecifier = table.getCellContents(1, rowIndex).toLowerCase();
      tableTypeArrays.put(fixture, makeTableType(tableSpecifier));
    }
  }

  public void addDefaultTableType(String fixture, String tableType) {
	 tableTypeArrays.put(fixture, tableType);
  }

  public void addAlias(String alias, String fixture) {
    String disgracedAlias = Disgracer.disgraceClassName(alias);
    aliasArrays.put(disgracedAlias, fixture);
  }

  private String makeTableType(String tableSpecifier) {
    tableSpecifier = tableSpecifier.replace(':', ' ');
    if (tableSpecifier.startsWith("as"))
    	tableSpecifier = tableSpecifier.substring(2);

    return tableSpecifier.trim();
  }

  private void parseDefineAliasTable(Table table) {
    for (int rowIndex = 1; rowIndex < table.getRowCount(); rowIndex++)
      parseDefineAliasRow(table, rowIndex);
  }

  private void parseDefineAliasRow(Table table, int rowIndex) {
    if (table.getColumnCountInRow(rowIndex) >= 2) {
      String fixtureName = table.getCellContents(0, rowIndex);
      String tableSpecifier = table.getCellContents(1, rowIndex).trim();
      addAlias(fixtureName, tableSpecifier);
    }
  }

  public SlimTableFactory copy() {
    return new SlimTableFactory(new HashMap<>(tableTypes),
            new HashMap<>(tableTypeArrays),
            new HashMap<>(aliasArrays));
  }

  private static class TableTypeAndName {
    private String tableType;
    private String tableName;

    private TableTypeAndName(final String tableType, final String tableName) {
      this.tableType = tableType.toLowerCase().trim();
      this.tableName = tableName.trim();
    }

    private TableTypeAndName(final String[] typeAndName) {
      this(typeAndName[0], typeAndName.length > 1 ? typeAndName[1] : "");
    }

    private boolean hasTableType(final String name) {
      return tableType.equalsIgnoreCase(name);
    }

    private boolean hasTableName(final String name) {
      return tableName.equalsIgnoreCase(name);
    }

    private boolean mayBeDecisionTable() {
      return StringUtils.isBlank(tableType) && !StringUtils.isBlank(tableName);
    }
  }
}
