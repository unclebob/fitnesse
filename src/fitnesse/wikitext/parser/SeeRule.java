package fitnesse.wikitext.parser;

import util.Maybe;

public class SeeRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.WikiWord)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(next));
    }
}
