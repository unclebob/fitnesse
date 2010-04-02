package fitnesse.wikitext.parser;

import util.Maybe;

public class PreformatRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol body = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.ClosePreformat);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Preformat).add(body));
    }
}
