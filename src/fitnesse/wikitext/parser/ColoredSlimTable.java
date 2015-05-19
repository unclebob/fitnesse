package fitnesse.wikitext.parser;

import fit.FixtureLoader;
import fit.FixtureName;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColoredSlimTable extends SymbolTypeDecorator {

  public ColoredSlimTable(Table baseSymbolType) {
    super("Table", baseSymbolType);

    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DynamicDecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.QueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.SubsetQueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.OrderedQueryTable");
  }

  private Set<String> secondRowTitleClasses = new HashSet<>();

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
    tableDecorator.onHeaderStarts(writer);
    Table table = (Table) baseSymbolType;
    int longestRow = table.longestRow(symbol);

    List<Symbol> children = symbol.getChildren();
    for (int row = 0; row < children.size(); row++) {
      Symbol child = children.get(row);
      writer.startTag("tr");
      if (row == 0 && symbol.hasProperty("hideFirst")) {
        writer.putAttribute("class", "hidden");
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
        tableDecorator.onHeaderEnds(writer);
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
    if (symbol.hasProperty("class")) {
      writer.putAttribute("class", symbol.getProperty("class"));
    } else if (className != null) {
      writer.putAttribute("class", className);
    }
  }

  private String getFirstCell(Translator translator, Symbol symbol) {
    return ((Table) baseSymbolType).translateCellBody(translator,
      symbol.getChildren().get(0).getChildren().get(0));
  }

  private Set<String> getTableNamesForDataTables() {
    return new HashSet<>(Arrays.asList("fitnesse.testsystems.slim.tables.DecisionTable"));
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
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
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
      } else if (tableClassName.equals("fitnesse.testsystems.slim.tables.ScriptTable") ||
        tableClassName.equals("fitnesse.testsystems.slim.tables.ScenarioTable")) {
        return new ColoredEveryRowTableDecorator();
      } else {
        return new ColoredTableDecorator();
      }
    }
    Table table = (Table) baseSymbolType;
    int longestRow = table.longestRow(symbol);
    if (longestRow == table.rowLength(tableDescription.isSecondRowTitle ? symbol.getChildren().get(1) : symbol.getChildren().get(0))) {
      return new DataTableDecorator();
    } else {
      return new ColoredTableDecorator();
    }
  }


  private TableDescription getTableDescription(Class<?> tableClass) {
    TableDescription tableDescription = new TableDescription();
    // If is slim table class declaration then get fixture info for table coloring scheme.
    if (SlimTable.class.isAssignableFrom(tableClass)) {
      if (secondRowTitleClasses.contains(tableClass.getName())) {
        tableDescription.isSecondRowTitle = true;
      } else if (tableClass.getName().equals("fitnesse.testsystems.slim.tables.ImportTable")) {
        tableDescription.isImportFixture = true;
      }
    }
    // Unmarked decision tables aren't found by getTableType().  Color table if first row is valid class.
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
    if (testSystem.isNothing() || !testSystem.getValue().equals("slim")) {
      return baseSymbolType;
    }
    return this;
  }

  private static class TableDescription {
    boolean isImportFixture = false;
    boolean isSecondRowTitle = false;
  }

  abstract static class TableDecorator {
    abstract void decorateRow(HtmlWriter writer, String body);

    void onHeaderStarts(HtmlWriter writer) {
    }

    void onHeaderEnds(HtmlWriter writer) {
    }

    void decorateHeaderRow(HtmlWriter writer) {
      writer.putAttribute("class", "slimRowTitle");
    }

    public String getClassForTable() {
      return null;
    }
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
