package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol current = scanner.getCurrent();
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Text)) return Symbol.Nothing;
        return new Maybe<Symbol>(current.add(scanner.getCurrent()));
    }
}
