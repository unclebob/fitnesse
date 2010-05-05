package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        SymbolType type = scanner.getCurrentType();
        Symbol body = parser.parseToIgnoreFirst(type);
        if (scanner.isEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(new Symbol(type).add(body));
    }
}
