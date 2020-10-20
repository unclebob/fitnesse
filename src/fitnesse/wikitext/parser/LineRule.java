package fitnesse.wikitext.parser;

public class LineRule implements Rule {
  public static final String LEVEL = "level";
  public static final String ID = "id";

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        String level = current.getContent().substring(1,2);
        if (ScanString.isDigits(level)) { //todo: a bit of a hack - this is for header lines
          current.putProperty(LEVEL, level);
          current.putProperty(ID, Integer.toString(parser.getPage().nextId()));
        }

        current.add(parser.parseToEnd(SymbolType.Newline));
        if (parser.peek().isType(SymbolType.Newline) && !parser.endsOn(SymbolType.Newline)) parser.moveNext(1);
        return new Maybe<>(current);
    }
}
