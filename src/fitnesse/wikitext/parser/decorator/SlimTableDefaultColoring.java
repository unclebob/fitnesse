package fitnesse.wikitext.parser.decorator;

import fit.FixtureLoader;
import fit.FixtureName;
import fitnesse.testsystems.slim.tables.DecisionTable;
import fitnesse.testsystems.slim.tables.DynamicDecisionTable;
import fitnesse.testsystems.slim.tables.ImportTable;
import fitnesse.testsystems.slim.tables.QueryTable;
import fitnesse.testsystems.slim.tables.ScenarioTable;
import fitnesse.testsystems.slim.tables.ScriptTable;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.util.CacheHelper;
import fitnesse.util.ClassUtils;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.Table;
import fitnesse.wikitext.parser.VariableSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static fitnesse.wikitext.parser.decorator.SymbolClassPropertyAppender.classPropertyAppender;
import static fitnesse.wikitext.parser.decorator.SymbolInspector.inspect;

public class SlimTableDefaultColoring implements ParsedSymbolDecorator {

  private static final SlimTableDefaultColoring INSTANCE = new SlimTableDefaultColoring();

  private static boolean isInstalled;

  public static void install() {
    if (!isInstalled) {
      Table.symbolType.addDecorator(INSTANCE);
      isInstalled = true;
    }
  }

  public static void uninstall() {
    Table.symbolType.removeDecorator(INSTANCE);
    isInstalled = false;
  }

  //visible for testing
  SlimTableDefaultColoring() {
    //hidden
  }

  private final Map<String, Boolean> validClasses = Collections.synchronizedMap(CacheHelper.lruCache(1000));

  @Override
  public void handleParsedSymbol(Symbol symbol, VariableSource variableSource) {
    if (isSlimContext(variableSource)) {
      inspect(symbol).checkSymbolType(Table.symbolType);
      handleParsedTable(symbol);
    }
  }

  private void handleParsedTable(Symbol table) {
    boolean isImportFixture = false;
    boolean colorTable = false;
    boolean isFirstColumnTitle = false;
    boolean isSecondRowTitle = false;

    int rowNo = 0;
    for (Symbol row : table.getChildren()) {
      rowNo++;
      List<Symbol> columns = row.getChildren();
      if (!columns.isEmpty()) {
        Symbol firstCell = columns.get(0);
        final String cellContent = inspect(firstCell).getRawContent();

        if (rowNo == 1) {
          // If slim table class declaration then get fixture info for table coloring scheme
          SlimTableFactory sf = new SlimTableFactory();
          Class<? extends SlimTable> slimTableClazz = sf.getTableType(cellContent);
          if (slimTableClazz != null) {
            colorTable = true;
            if (DecisionTable.class.isAssignableFrom(slimTableClazz) ||
              DynamicDecisionTable.class.isAssignableFrom(slimTableClazz) ||
              QueryTable.class.isAssignableFrom(slimTableClazz)) {
              isSecondRowTitle = true;
            } else if (ImportTable.class.isAssignableFrom(slimTableClazz)) {
              isImportFixture = true;
            } else if (ScriptTable.class.isAssignableFrom(slimTableClazz) ||
              ScenarioTable.class.isAssignableFrom(slimTableClazz)) {
              isFirstColumnTitle = true;
            }
          }

          // Unmarked decision tables aren't found by getTableType().  Color table if first row is valid class.
          if (!colorTable) {
            List<String> potentialClasses = new FixtureName(cellContent)
              .getPotentialFixtureClassNames(FixtureLoader.instance().fixturePathElements);
            for (String potentialClass : potentialClasses) {
              if (isValidClass(potentialClass)) {
                colorTable = true;
                isSecondRowTitle = true;
                break;
              }
            }
          }
        }

        // Use color scheme attributes to color table rows.
        if (colorTable) {
          if (isImportFixture) {
            FixtureLoader.instance().addPackageToPath(cellContent);
          }

          if (rowNo == 1) {
            classPropertyAppender().addPropertyValue(row, "slimRowTitle");
          } else if (isSecondRowTitle && rowNo == 2) {
            classPropertyAppender().addPropertyValue(row, "slimRowTitle");
          } else if (isFirstColumnTitle) {
            byte[] bodyBytes = cellContent.getBytes();
            int sum = 0;
            for (byte b : bodyBytes) {
              sum = sum + (int) b;
            }
            classPropertyAppender().addPropertyValue(row, "slimRowColor" + (sum % 10));
          } else {
            classPropertyAppender().addPropertyValue(row, "slimRowColor" + (rowNo % 2));
          }
        }
      }
    }
  }

  private boolean isValidClass(String potentialClass) {
    return validClasses.computeIfAbsent(potentialClass, p -> {
      try {
        return ClassUtils.forName(potentialClass) != null;
      } catch (Exception | NoClassDefFoundError e) {
        return false;
      }
    });
  }

  private boolean isSlimContext(VariableSource variableSource) {
    Maybe<String> testSystem = variableSource.findVariable("TEST_SYSTEM");
    return "slim".equals(testSystem.getValue());
  }
}
