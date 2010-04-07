package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol body = Parser.make(getPage(), scanner, SymbolType.Newline).parse();
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Symbol(SymbolType.List).add(body));
    }
}
