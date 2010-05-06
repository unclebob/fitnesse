package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(parser.parseWithEnds(new SymbolType[] {SymbolType.Newline})));
    }

}
