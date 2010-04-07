package fitnesse.wikitext.parser;

import util.Maybe;

public class EqualPairRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        SymbolType type = scanner.getCurrentType();
        Symbol body = Parser.makeIgnoreFirst(getPage(), scanner, type).parse();
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(type).add(body));
    }
}
