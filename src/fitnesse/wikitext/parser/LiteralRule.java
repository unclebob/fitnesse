package fitnesse.wikitext.parser;

import util.Maybe;

public class LiteralRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        scanner.makeLiteral(SymbolType.CloseLiteral);
        if (scanner.isEnd()) return Symbol.Nothing;
        String literal = scanner.getCurrentContent();
        scanner.moveNext();
        return new Maybe<Symbol>(new Symbol(SymbolType.Literal, literal));
    }
}
