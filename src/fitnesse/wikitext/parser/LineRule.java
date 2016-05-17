package fitnesse.wikitext.parser;

public class LineRule implements Rule {
    public static final String Level = "level";

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        String level = current.getContent().substring(1,2);
        if (ScanString.isDigits(level)) current.putProperty(Level, level);

        current.add(parser.parseToEnd(SymbolType.Newline));
        if (parser.peek().isType(SymbolType.Newline) && !parser.endsOn(SymbolType.Newline)) parser.moveNext(1);
        return new Maybe<>(current);
    }
}
