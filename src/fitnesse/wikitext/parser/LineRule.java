package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule implements Rule {
    public static final String Level = "level";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        String level = current.getContent().substring(1,2);
        if (ScanString.isDigits(level)) current.putProperty(Level, level);

        return new Maybe<Symbol>(current.add(parser.parseToEnd(SymbolType.Newline)));
    }

}
