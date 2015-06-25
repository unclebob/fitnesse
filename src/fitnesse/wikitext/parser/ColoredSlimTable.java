package fitnesse.wikitext.parser;

import fit.FixtureLoader;
import fit.FixtureName;
import fitnesse.testsystems.slim.tables.DecisionTable;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColoredSlimTable extends SymbolTypeDecorator {

    public static final String CLASS_PROPERTY = "class";

    public ColoredSlimTable(Table baseSymbolType) {
    super("Table", baseSymbolType);

    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DynamicDecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.QueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.SubsetQueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.OrderedQueryTable");
  }

  private Set<String> secondRowTitleClasses = new HashSet<String>();

  public String toTarget(Translator translator, Symbol symbol) {
    HtmlWriter writer = new HtmlWriter();
    writer.startTag("table");
    String tableName = getFirstCell(translator, symbol);
    Class<?> tableClass = getClassForTable(tableName);
    TableDecorator tableDecorator;
    TableDescription tableDescription;
    if (tableClass != null) {
      tableDescription = getTableDescription(tableClass);
      tableDecorator = getDecorator(tableClass, tableDescription, symbol);
    } else {
      tableDecorator = new EmptyDecorator();
      tableDescription = new TableDescription();
    }
    setClass(symbol, writer, tableDecorator.getClassForTable());
    writer.startTag("thead");
    Table table = (Table) baseSymbolType;
    int longestRow = table.longestRow(symbol);

    List<Symbol> children = symbol.getChildren();
    for (int row = 0; row < children.size(); row++) {
      Symbol child = children.get(row);
      writer.startTag("tr");
      if (row == 0 && symbol.hasProperty("hideFirst")) {
        writer.putAttribute(CLASS_PROPERTY, "hidden");
      }
      int extraColumnSpan = longestRow - table.rowLength(child);
      List<Symbol> children1 = child.getChildren();
      for (int column = 0; column < children1.size(); column++) {
        Symbol grandChild = children1.get(column);
        String body = table.translateCellBody(translator, grandChild);

        // Use color scheme attributes to color table rows.
        if (column == 0) {
          processRow(writer, tableDescription, row, body, tableDecorator);
        }
        writer.startTag("td");
        if (extraColumnSpan > 0 && column == table.rowLength(child) - 1)
          writer.putAttribute("colspan", Integer.toString(extraColumnSpan + 1));
        writer.putText(body);
        writer.endTag();
      }
      writer.endTag();
      if ((row == 0 && !tableDescription.isSecondRowTitle) ||
        (row == 1 && tableDescription.isSecondRowTitle)) {
        writer.endTag();//end thead
      }
    }
    writer.endTag();
    return writer.toHtml();
  }

  private void processRow(HtmlWriter writer, TableDescription tableDescription, int rowCount, String body,
                          TableDecorator tableDecorator) {
    if (tableDescription.isImportFixture) {
      FixtureLoader.instance().addPackageToPath(body);
    }

    if (rowCount == 0 || (tableDescription.isSecondRowTitle && rowCount == 1)) {
      tableDecorator.decorateHeaderRow(writer);
    } else {
      tableDecorator.decorateRow(writer, body);
    }
  }

  private void setClass(Symbol symbol, HtmlWriter writer, String className) {
    if (symbol.hasProperty(CLASS_PROPERTY)) {
      writer.putAttribute(CLASS_PROPERTY, symbol.getProperty(CLASS_PROPERTY));
    } else if (className != null) {
      writer.putAttribute(CLASS_PROPERTY, className);
    }
  }

  private String getFirstCell(Translator translator, Symbol symbol) {
    return ((Table) baseSymbolType).translateCellBody(translator,
      symbol.getChildren().get(0).getChildren().get(0));
  }

  private Set<String> getTableNamesForDataTables() {
    return (Set<String>)System.getProperties().get("DataTablesClasses");
  }

  private Class<?> getClassForTable(String tableName) {
    SlimTableFactory sf = new SlimTableFactory();
    Class<? extends SlimTable> slimTableClazz = sf.getTableType(tableName);
    if (slimTableClazz != null) {
      return slimTableClazz;
    }
    // Unmarked decision tables aren't found by getTableType().  Color table if first row is valid class.
    else {
      List<String> potentialClasses = new FixtureName(tableName)
        .getPotentialFixtureClassNames(FixtureLoader.instance().fixturePathElements);
      for (String potentialClass : potentialClasses) {
        try {
          Class<?> fixtureClazz = Class.forName(potentialClass);
          if (fixtureClazz != null) {
            return fixtureClazz;
          }
        } catch (ClassNotFoundException ignored) {
        } catch (NoClassDefFoundError ignored) {
        }
      }
    }
    return null;
  }

  private TableDecorator getDecorator(Class<?> tableClass, TableDescription tableDescription, Symbol symbol) {
    if (SlimTable.class.isAssignableFrom(tableClass)) {
      String tableClassName = tableClass.getName();
      if (getTableNamesForDataTables().contains(tableClassName)) {
        return new DataTableDecorator();
      } else if ("fitnesse.testsystems.slim.tables.ScriptTable".equals(tableClassName) ||
          "fitnesse.testsystems.slim.tables.ScenarioTable".equals(tableClassName)) {
        return new ColoredEveryRowTableDecorator();
      } else {
        return new ColoredTableDecorator();
      }
    }
    //reaching this line means that we are creating table for DecisionTable but without explicit table type declaration
    //Like in "Should I buy milk" example
    if (!getTableNamesForDataTables().contains(DecisionTable.class.getName()))
    {
      return new ColoredTableDecorator();
    }
    Table table = (Table) baseSymbolType;
    int longestRow = table.longestRow(symbol);
    List<Symbol> rows = symbol.getChildren();
    if (longestRow == table.rowLength(tableDescription.isSecondRowTitle && rows.size() > 1 ? rows.get(1) : rows.get(0))) {
      return new DataTableDecorator();
    } else {
      return new ColoredTableDecorator();
    }
  }

  private TableDescription getTableDescription(Class<?> tableClass) {
    TableDescription tableDescription = new TableDescription();
    if (SlimTable.class.isAssignableFrom(tableClass)) {
      if (secondRowTitleClasses.contains(tableClass.getName())) {
        tableDescription.isSecondRowTitle = true;
      } else if ("fitnesse.testsystems.slim.tables.ImportTable".equals(tableClass.getName())) {
        tableDescription.isImportFixture = true;
      }
    }
    else {
      tableDescription.isSecondRowTitle = true;
    }
    return tableDescription;
  }

  @Override
  public SymbolType isApplicable(Translator translator) {
    Maybe<String> testSystem = Maybe.noString;
    if (translator instanceof HtmlTranslator) {
      testSystem = ((HtmlTranslator) translator).getParsingPage().findVariable("TEST_SYSTEM");
    }
    if (testSystem.isNothing() || !"slim".equals(testSystem.getValue())) {
      return baseSymbolType;
    }
    return this;
  }

  private static class TableDescription {
    boolean isImportFixture = false;
    boolean isSecondRowTitle = false;
  }

  static class EmptyDecorator extends TableDecorator {
    @Override
    void decorateRow(HtmlWriter writer, String body) {
    }

    @Override
    void decorateHeaderRow(HtmlWriter writer) {
    }
  }

}
