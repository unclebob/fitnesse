package fitnesse.wikitext.parser;

import util.Maybe;

public class HashTableRule implements Rule {
    private static final SymbolType[] terminators = new SymbolType[] {SymbolType.Colon, SymbolType.Comma, SymbolType.CloseBrace};

    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol table = new Symbol(SymbolType.HashTable);
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            table.add(row);
            for (int i = 0; i < 2; i++) {
                Symbol cell = parser.parseToIgnoreFirst(terminators);
                if (scanner.isEnd()) return Symbol.Nothing;
                row.add(cell);
            }
            if (scanner.isType(SymbolType.CloseBrace)) break;
        }
        return new Maybe<Symbol>(table);
    }
}
