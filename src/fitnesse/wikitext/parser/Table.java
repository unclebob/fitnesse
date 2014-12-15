package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

import fit.FixtureLoader;
import fit.FixtureName;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

public class Table extends SymbolType implements Rule, Translation {
  public static final Table symbolType = new Table();
  private List<String> secondRowTitleClasses = new ArrayList<String>();

  public Table() {
    super("Table");
    wikiMatcher(new Matcher().startLine().string("|"));
    wikiMatcher(new Matcher().startLine().string("!|"));
    wikiMatcher(new Matcher().startLine().string("-|"));
    wikiMatcher(new Matcher().startLine().string("-!|"));
    wikiRule(this);
    htmlTranslation(this);

    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.DynamicDecisionTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.QueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.SubsetQueryTable");
    secondRowTitleClasses.add("fitnesse.testsystems.slim.tables.OrderedQueryTable");
  }

  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    String content = current.getContent();
    if (content.charAt(0) == '-') current.putProperty("hideFirst", "");
    boolean endOfTable = false;
    while (!endOfTable) {
      Symbol row = new Symbol(SymbolType.SymbolList);
      current.add(row);
      while (true) {
        int offset = parser.getOffset();
        Symbol cell = parseCell(parser, content);
        if (parser.getOffset() == offset) {
          endOfTable = true;
          break;
        }
        if (parser.atEnd()) return Symbol.nothing;
        if (containsNewLine(cell)) return Symbol.nothing;
        row.add(cell);
        if (endsRow(parser.getCurrent())) break;
      }
      if (!startsRow(parser.getCurrent())) break;
    }
    return new Maybe<Symbol>(current);
  }

  private Symbol parseCell(Parser parser, String content) {
    return (content.contains("!"))
      ? parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableProvider, ParseSpecification.tablePriority)
      : parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.tableParsingProvider, ParseSpecification.tablePriority);
  }

  private boolean containsNewLine(Symbol cell) {
    for (Symbol child : cell.getChildren()) {
      if (child.isType(SymbolType.Newline)) return true;
    }
    return false;
  }

  private boolean endsRow(Symbol symbol) {
    return symbol.getContent().indexOf("\n") > 0;
  }

  private boolean startsRow(Symbol symbol) {
    return symbol.getContent().contains("\n|");
  }

  public String toTarget(Translator translator, Symbol symbol) {
    HtmlWriter writer = new HtmlWriter();
    writer.startTag("table");
    if (symbol.hasProperty("class")) {
      writer.putAttribute("class", symbol.getProperty("class"));
    }
    int longestRow = longestRow(symbol);
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
      int extraColumnSpan = longestRow - rowLength(child);
      int column = 1;
      for (Symbol grandChild : child.getChildren()) {
        String body = translateCellBody(translator, grandChild);

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

            // If table has valid class declaration then color table and choose coloring scheme.
            List<String> potentialClasses = new FixtureName(tableName)
                .getPotentialFixtureClassNames(FixtureLoader.instance().fixturePathElements);
            for(String potentialClass: potentialClasses){
                if(potentialClass.equals("fitnesse.testutil.CrashFixture")) continue;
                Object fixture;
                Class<?> fixtureClazz;
                try{
                    if((fixtureClazz = Class.forName(potentialClass)) != null){
                        colorTable = true;

                        // Attempt to instantiate class to get inheritance.
                        fixture = fixtureClazz.newInstance();
                        if(fixture instanceof fit.Comment){ isCommentFixture = true; }
                        if(fixture instanceof fit.ImportFixture){ isImportFixture = true; }
                        if(fixture instanceof fit.ActionFixture){
                            isSecondRowTitle = true;
                            isFirstColumnTitle = true;
                        }
                        if(fixture instanceof fit.ColumnFixture){ isSecondRowTitle = true; }
                    }
                }catch(ClassNotFoundException cnfe){ }
                catch(IllegalAccessException iae){ }
                catch(InstantiationException iae){ }
                catch(NoClassDefFoundError ncdfe){ }
            }
        }

        // Use color scheme attributes to color table rows.
        if(colorTable && column == 1){
            if(isImportFixture){ FixtureLoader.instance().addPackageToPath(body); }

            if(rowCount == 1){
                writer.putAttribute("class", "rowTitle");
            }else if(isSecondRowTitle && rowCount == 2){
                writer.putAttribute("class", "rowTitle");
            }else if(isFirstColumnTitle){
                byte[] bodyBytes = body.getBytes();
                int sum = 0;
                for(byte b: bodyBytes){
                    sum = sum + (int) b;
                }
                writer.putAttribute("class", "rowColor" + (sum % 10));
            }else if(!isCommentFixture){
                writer.putAttribute("class", "rowColor" + (rowCount % 2));
            }
        }
        writer.startTag("td");
        if (extraColumnSpan > 0 && column == rowLength(child))
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

  private String translateCellBody(Translator translator, Symbol cell) {
    final String literalDelimiter = new String(new char[]{255, 1, 255});
    cell.walkPreOrder(new SymbolTreeWalker() {
      public boolean visit(Symbol node) {
        if (node.isType(Literal.symbolType)) {
          node.setContent(literalDelimiter + node.getContent() + literalDelimiter);
        }
        return true;
      }

      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return translator.translate(cell).trim().replace(literalDelimiter, "");
  }

  private int longestRow(Symbol table) {
    int longest = 0;
    for (Symbol row : table.getChildren()) {
      int length = rowLength(row);
      if (length > longest) longest = length;
    }
    return longest;
  }

  private int rowLength(Symbol row) {
    return row.getChildren().size();
  }
}
