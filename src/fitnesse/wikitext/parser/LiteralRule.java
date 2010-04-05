package fitnesse.wikitext.parser;

import util.Maybe;

public class LiteralRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        SymbolType type = scanner.getCurrentType();
        SymbolType terminator = scanner.makeLiteral(SymbolType.closeType(type));
        if (terminator == SymbolType.Empty) return Symbol.Nothing;
        String literal = scanner.getCurrentContent();
        scanner.moveNext();
        return new Maybe<Symbol>(new Symbol(type, literal));
    }
}
