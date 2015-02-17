package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

import fit.FixtureLoader;
import fit.FixtureName;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

public class ColoredSlimTable extends SymbolTypeDecorator{

    public ColoredSlimTable(Table baseSymbolType) {
        super("Table", baseSymbolType);

        secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DecisionTable");
        secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DynamicDecisionTable");
        secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.QueryTable");
        secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.SubsetQueryTable");
        secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.OrderedQueryTable");
    }

    private List<String> secondRowTitleClasses = new ArrayList<String>();

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("table");
        if (symbol.hasProperty("class")) {
          writer.putAttribute("class", symbol.getProperty("class"));
        }
        int longestRow = ((Table)baseSymbolType).longestRow(symbol);
        int rowCount = 0;
        boolean isImportFixture = false;
        boolean colorTable = false;
        boolean isFirstColumnTitle = false;
        boolean isSecondRowTitle = false;
        boolean isCommentFixture = false;

        for (Symbol child : symbol.getChildren()) {
          rowCount++;
          writer.startTag("tr");
          if (rowCount == 1 && symbol.hasProperty("hideFirst")) {
            writer.putAttribute("class", "hidden");
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
                    if(secondRowTitleClasses.contains(slimTableClazz.getName())){
                        isSecondRowTitle = true;
                    }else if(slimTableClazz.getName().equals("fitnesse.testsystems.slim.tables.ImportTable")){
                        isImportFixture = true;
                    }else if(slimTableClazz.getName().equals("fitnesse.testsystems.slim.tables.ScriptTable") ||
                            slimTableClazz.getName().equals("fitnesse.testsystems.slim.tables.ScenarioTable")){
                        isFirstColumnTitle = true;
                    }
                }

                // Unmarked decision tables aren't found by getTableType().  Color table if first row is valid class.
                if(!colorTable) {
                    List<String> potentialClasses = new FixtureName(tableName)
                        .getPotentialFixtureClassNames(FixtureLoader.instance().fixturePathElements);
                    for(String potentialClass: potentialClasses){
                        Class<?> fixtureClazz;
                        try{
                            fixtureClazz = Class.forName(potentialClass);
                            if(fixtureClazz == null){ continue; }
                            colorTable = true;
                            isSecondRowTitle = true;
                        }catch(ClassNotFoundException cnfe){ }
                        catch(NoClassDefFoundError ncdfe){ }
                    }
                }
            }

            // Use color scheme attributes to color table rows.
            if(colorTable && column == 1){
                if(isImportFixture){ FixtureLoader.instance().addPackageToPath(body); }

                if(rowCount == 1){
                    writer.putAttribute("class", "slimRowTitle");
                }else if(isSecondRowTitle && rowCount == 2){
                    writer.putAttribute("class", "slimRowTitle");
                }else if(isFirstColumnTitle){
                    byte[] bodyBytes = body.getBytes();
                    int sum = 0;
                    for(byte b: bodyBytes){
                        sum = sum + (int) b;
                    }
                    writer.putAttribute("class", "slimRowColor" + (sum % 10));
                }else if(!isCommentFixture){
                    writer.putAttribute("class", "slimRowColor" + (rowCount % 2));
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

    public SymbolType isApplicable(Translator translator){
        Maybe<String> testSystem = Maybe.noString;
        if(translator instanceof HtmlTranslator){
            testSystem = ((HtmlTranslator) translator).getParsingPage().findVariable("TEST_SYSTEM");
        }
        if(testSystem.isNothing() || !testSystem.getValue().equals("slim")){
            return baseSymbolType;
        }
        return this;
    }
}
