package fitnesse.wikitext.parser;

public class Table extends SymbolType implements Rule, Translation {
  public static final Table symbolType = new Table();

  public static final SymbolType tableRow = new SymbolType("TableRow");
  public static final SymbolType tableCell = new SymbolType("TableCell");

  public Table() {
    super("Table");
    wikiMatcher(new Matcher().startLine().string("|"));
    wikiMatcher(new Matcher().startLine().string("!|"));
    wikiMatcher(new Matcher().startLine().string("-|"));
    wikiMatcher(new Matcher().startLine().string("-!|"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    String content = current.getContent();
    if (content.charAt(0) == '-') current.putProperty("hideFirst", "");
    boolean endOfTable = false;
    while (!endOfTable) {
      Symbol row = new Symbol(tableRow);
      row.setStartOffset(parser.getOffset());
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
      row.setEndOffset(parser.getOffset());
      if (!startsRow(parser.getCurrent())) break;
    }
    return new Maybe<>(current);
  }

  private Symbol parseCell(Parser parser, String content) {
    Symbol cell = (content.contains("!"))
            ? parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableProvider, ParseSpecification.tablePriority)
            : parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.tableParsingProvider, ParseSpecification.tablePriority);
    cell.setType(tableCell);
    return cell;
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

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
      HtmlWriter writer = new HtmlWriter();
      writer.startTag("table");
      if (symbol.hasProperty("class")) {
        writer.putAttribute("class", symbol.getProperty("class"));
      }
      int longestRow = longestRow(symbol);
      int rowCount = 0;
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

  protected String translateCellBody(Translator translator, Symbol cell) {
    final String literalDelimiter = new String(new char[]{255, 1, 255});
    cell.walkPreOrder(new SymbolTreeWalker() {
      @Override
      public boolean visit(Symbol node) {
        if (node.isType(Literal.symbolType)) {
          node.setContent(literalDelimiter + node.getContent() + literalDelimiter);
        }
        return true;
      }

      @Override
      public boolean visitChildren(Symbol node) {
        return true;
      }
    });
    return translator.translate(cell).trim().replace(literalDelimiter, "");
  }

  protected int longestRow(Symbol table) {
    int longest = 0;
    for (Symbol row : table.getChildren()) {
      int length = rowLength(row);
      if (length > longest) longest = length;
    }
    return longest;
  }

  protected int rowLength(Symbol row) {
    return row.getChildren().size();
  }
}
