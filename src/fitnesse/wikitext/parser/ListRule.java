package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol body = new Parser(getPage()).parse(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.List).add(body));
    }
}
