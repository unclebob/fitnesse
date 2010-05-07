package fitnesse.wikitext.parser;

import util.Maybe;

public class HashTableRule implements Rule {
    private static final SymbolType[] terminators = new SymbolType[] {SymbolType.Colon, SymbolType.Comma, SymbolType.CloseBrace};

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        while (true) {
            Symbol row = new Symbol(SymbolType.HashRow);
            current.add(row);
            for (int i = 0; i < 2; i++) {
                Symbol cell = parser.parseToIgnoreFirst(terminators);
                if (parser.atEnd()) return Symbol.nothing;
                row.add(cell);
            }
            if (parser.getCurrent().isType(SymbolType.CloseBrace)) break;
        }
        return new Maybe<Symbol>(current);
    }
}
