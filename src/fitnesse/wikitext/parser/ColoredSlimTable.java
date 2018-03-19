package fitnesse.wikitext.parser;

import java.util.List;

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
import fitnesse.util.ClassUtils;

public class ColoredSlimTable extends SymbolTypeDecorator{

  public static final String CLASS_PROPERTY = "class";

  public ColoredSlimTable(Table baseSymbolType) {
        super("Table", baseSymbolType);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("table");
        if (symbol.hasProperty(CLASS_PROPERTY)) {
          writer.putAttribute(CLASS_PROPERTY, symbol.getProperty(CLASS_PROPERTY));
        }
        int longestRow = ((Table)baseSymbolType).longestRow(symbol);
        int rowCount = 0;
        boolean isImportFixture = false;
        boolean colorTable = false;
        boolean isFirstColumnTitle = false;
        boolean isSecondRowTitle = false;

        for (Symbol child : symbol.getChildren()) {
          rowCount++;
          writer.startTag("tr");
          if (rowCount == 1 && symbol.hasProperty("hideFirst")) {
            writer.putAttribute(CLASS_PROPERTY, "hidden");
          }
          int extraColumnSpan = longestRow - ((Table)baseSymbolType).rowLength(child);
          int column = 1;
          for (Symbol grandChild : child.getChildren()) {
            String body = ((Table)baseSymbolType).translateCellBody(translator, grandChild);

            if(rowCount == 1 && column == 1){
                String tableName = body;

                // If is slim table class declaration then get fixture info for table coloring scheme.
                SlimTableFactory sf = new SlimTableFactory();
                Class<? extends SlimTable> slimTableClazz = sf.getTableType(tableName);
                if(slimTableClazz != null){
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
                if(!colorTable) {
                    List<String> potentialClasses = new FixtureName(tableName)
                        .getPotentialFixtureClassNames(FixtureLoader.instance().fixturePathElements);
                    for(String potentialClass: potentialClasses){
                        if (isValidClass(potentialClass)) {
                          colorTable = true;
                          isSecondRowTitle = true;
                          break;
                        }
                    }
                }
            }

            // Use color scheme attributes to color table rows.
            if(colorTable && column == 1){
                if(isImportFixture){ FixtureLoader.instance().addPackageToPath(body); }

                if(rowCount == 1){
                    writer.putAttribute(CLASS_PROPERTY, "slimRowTitle");
                }else if(isSecondRowTitle && rowCount == 2){
                    writer.putAttribute(CLASS_PROPERTY, "slimRowTitle");
                }else if(isFirstColumnTitle){
                    byte[] bodyBytes = body.getBytes();
                    int sum = 0;
                    for(byte b: bodyBytes){
                        sum = sum + (int) b;
                    }
                    writer.putAttribute(CLASS_PROPERTY, "slimRowColor" + (sum % 10));
                } else {
                    writer.putAttribute(CLASS_PROPERTY, "slimRowColor" + (rowCount % 2));
                }
            }
            writer.startTag("td");
            if (extraColumnSpan > 0 && column == ((Table)baseSymbolType).rowLength(child))
              writer.putAttribute("colspan", Integer.toString(extraColumnSpan + 1));
            writer.putText(body);
            writer.endTag();
            column++;
          }
          writer.endTag();
        }
        writer.endTag();
        return writer.toHtml();
    }

  private boolean isValidClass(String potentialClass) {
    try {
      return ClassUtils.forName(potentialClass) != null;
    } catch (Exception|NoClassDefFoundError e) {
      return false;
    }
  }

  @Override
  public SymbolType isApplicable(Translator translator){
        Maybe<String> testSystem = Maybe.noString;
        if(translator instanceof HtmlTranslator){
            testSystem = ((HtmlTranslator) translator).getParsingPage().findVariable("TEST_SYSTEM");
        }
        if(testSystem.isNothing() || !"slim".equals(testSystem.getValue())) {
            return baseSymbolType;
        }
        return this;
    }
}
