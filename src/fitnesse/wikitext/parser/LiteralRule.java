package fitnesse.wikitext.parser;

import util.Maybe;

public class LiteralRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        SymbolType type = current.getType();
        String literal = parser.parseLiteral(type.closeType());
        if (parser.atEnd())  return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(type, literal));
    }
}
