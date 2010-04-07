package fitnesse.wikitext.parser;

import util.Maybe;

public class HashTableRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol table = new Symbol(SymbolType.HashTable);
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            table.add(row);
            for (int i = 0; i < 2; i++) {
                Symbol cell = Parser.makeIgnoreFirst(getPage(), scanner,
                        new SymbolType[] {SymbolType.Colon, SymbolType.Comma, SymbolType.CloseBrace})
                        .parse();
                if (scanner.isEnd()) return Symbol.Nothing;
                row.add(cell);
            }
            if (scanner.isType(SymbolType.CloseBrace)) break;
        }
        return new Maybe<Symbol>(table);
    }
}
