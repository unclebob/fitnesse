package fitnesse.wikitext.parser.decorator;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.tables.DecisionTable;
import fitnesse.testsystems.slim.tables.DynamicDecisionTable;
import fitnesse.testsystems.slim.tables.QueryTable;
import fitnesse.testsystems.slim.tables.ScenarioTable;
import fitnesse.testsystems.slim.tables.ScriptTable;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.PageData;
import fitnesse.wikitext.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.Table;
import fitnesse.wikitext.VariableSource;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fitnesse.wikitext.parser.decorator.SymbolClassPropertyAppender.classPropertyAppender;
import static fitnesse.wikitext.parser.decorator.SymbolInspector.inspect;
import static java.util.Arrays.asList;

public class SlimTableDefaultColoring implements ParsedSymbolDecorator {

  private static final Set<String> SPECIAL_PAGES =
    new HashSet<>(
      asList(WikiTestPage.SCENARIO_LIBRARY,
        WikiTestPage.SET_UP, WikiTestPage.TEAR_DOWN,
        PageData.SUITE_SETUP_NAME, PageData.SUITE_TEARDOWN_NAME));

  private static SlimTableDefaultColoring INSTANCE;

  private static boolean isInstalled;

  public static synchronized void createInstanceIfNeeded(SlimTableFactory factory) {
    if (INSTANCE == null) {
      INSTANCE = new SlimTableDefaultColoring(factory);
    }
  }

  public static void install() {
    if (!isInstalled) {
      if (INSTANCE == null) {
        throw new IllegalStateException("No table factory provided yet");
      }
      Table.symbolType.addDecorator(INSTANCE);
      isInstalled = true;
    }
  }

  public static void uninstall() {
    Table.symbolType.removeDecorator(INSTANCE);
    isInstalled = false;
  }

  private final SlimTableFactory sf;

  protected SlimTableDefaultColoring(SlimTableFactory factory) {
    sf = factory;
  }

  @Override
  public void handleParsedSymbol(Symbol symbol, VariableSource variableSource) {
    if ((isSlimContext(variableSource) && isOnTestPage(variableSource)) || isOnSpecialPage(variableSource)) {
      inspect(symbol).checkSymbolType(Table.symbolType);
      handleParsedTable(symbol);
    }
  }

  private void handleParsedTable(Symbol table) {
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
          Class<? extends SlimTable> slimTableClazz = sf.getTableType(cellContent);
          if (slimTableClazz != null) {
            colorTable = true;
            if (DecisionTable.class.isAssignableFrom(slimTableClazz) ||
              DynamicDecisionTable.class.isAssignableFrom(slimTableClazz) ||
              QueryTable.class.isAssignableFrom(slimTableClazz)) {
              isSecondRowTitle = true;
            } else if (ScriptTable.class.isAssignableFrom(slimTableClazz) ||
              ScenarioTable.class.isAssignableFrom(slimTableClazz)) {
              isFirstColumnTitle = true;
            }
          }

          // Unmarked decision tables aren't found by getTableType(), but they are Slim's default
          if (!colorTable) {
            String lowercaseContent = cellContent.toLowerCase();
            if (!lowercaseContent.equals("comment") && !lowercaseContent.startsWith("comment:")) {
              colorTable = true;
              isSecondRowTitle = true;
            }
          }
        }

        // Use color scheme attributes to color table rows.
        if (colorTable) {
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

  protected boolean isOnSpecialPage(VariableSource variableSource) {
    if (variableSource instanceof ParsingPage) {
      String name = ((ParsingPage) variableSource).getPage().getName();
      return SPECIAL_PAGES.contains(name);
    }
    return false;
  }

  protected boolean isOnTestPage(VariableSource variableSource) {
    if (variableSource instanceof ParsingPage) {
      return ((ParsingPage) variableSource).getPage().hasProperty("Test");
    }
    return false;
  }

  protected boolean isSlimContext(VariableSource parsingPage) {
    Optional<String> testSystem = parsingPage.findVariable("TEST_SYSTEM");
    return testSystem.isPresent() && "slim".equals(testSystem.get());
  }
}
