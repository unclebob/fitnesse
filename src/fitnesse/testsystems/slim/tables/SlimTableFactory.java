package fitnesse.testsystems.slim.tables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

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
    tableTypes.put(nameOrPrefix.toLowerCase().replaceAll(":", ""), tableClass);
  }

  public SlimTable makeSlimTable(Table table, String tableId, SlimTestContext slimTestContext) {
    SlimTable newTable;
    String tableType = getFullTableName(table.getCellContents(0, 0));
    //table.substitute(0, 0, tableType);

    // First the "exceptions to the rule"
    if ( tableType.equalsIgnoreCase("define alias")) {
      parseDefineAliasTable(table);
      return null;
    } else if (tableType.equalsIgnoreCase("define table type")) {
      parseDefineTableTypeTable(table);
      return null;
    } else if (tableType.equalsIgnoreCase("comment") || tableType.startsWith("comment:")) {
      return null;
    }

    Class<? extends SlimTable> tableClass = getTableType(tableType);

    if (tableClass != null) {
      newTable = newTableForType(tableClass, table, tableId, slimTestContext);
    } else if (!hasColon(tableType)) {
      newTable = new DecisionTable(table, tableId, slimTestContext);
    }else {
    	newTable = new SlimErrorTable(table, tableId, slimTestContext);
    }
    newTable.setFixtureName(getRawFixtureName(tableType));
    newTable.setTearDown(table.isTearDown());
    return newTable;
  }

  private boolean hasColon(String tableType) {
    return tableType.contains(":");
  }

  public String getRawTableTypeName(String fullTableName) {
	    if (hasColon(fullTableName)) {
	      return fullTableName.substring(0, fullTableName.indexOf(':')).trim().toLowerCase();
	    }
	    return "";
	  }

  public String getRawFixtureName(String fullTableName) {
	    if (hasColon(fullTableName)) {
	      return fullTableName.substring(fullTableName.indexOf(':') + 1).trim();
	    }
	    return fullTableName;
	  }

  public Class<? extends SlimTable> getTableType(String tableType) {
    if (hasColon(tableType)) {
      tableType = tableType.substring(0, tableType.indexOf(':'));
    }
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

  private String getFullTableName(String tableName) {
    String disgracedName = Disgracer.disgraceClassName(getRawFixtureName(tableName));
	  //check for an alias definition
    if (aliasArrays.containsKey(disgracedName)) {
      String fixtureName = aliasArrays.get(disgracedName);
      String tableType = getRawTableTypeName(tableName);
      if (hasColon(fixtureName)){
      	tableType = getRawTableTypeName(fixtureName);
      	fixtureName = getRawFixtureName(fixtureName);
        if (tableType.isEmpty()) tableType = getRawTableTypeName(tableName);
        if (fixtureName.isEmpty()) fixtureName = getRawFixtureName(tableName);
      }
      return tableType + ":" + fixtureName;
    }else if (hasColon(tableName)) {
    	// a table type definition exits in the table
      return tableName;
    }
      //check for a table type defined in a table type definition
    else if (tableTypeArrays.containsKey(disgracedName)) {
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

  private SlimTable parseDefineAliasTable(Table table) {
	    for (int rowIndex = 1; rowIndex < table.getRowCount(); rowIndex++)
	      parseDefineAliasRow(table, rowIndex);
	    return null;
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
}
