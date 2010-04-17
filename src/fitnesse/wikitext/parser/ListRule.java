package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol body = parser.parseTo(SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.List).add(body));
    }
}
