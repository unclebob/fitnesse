package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        SymbolType type = scanner.getCurrentType();
        Symbol body = parser.parseIgnoreFirst(type);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(type).add(body));
    }
}
