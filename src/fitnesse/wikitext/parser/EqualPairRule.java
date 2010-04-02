package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        SymbolType type = scanner.getCurrentType();
        Symbol body = new Parser(getPage()).parseIgnoreFirst(scanner, type);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(type).add(body));
    }
}
