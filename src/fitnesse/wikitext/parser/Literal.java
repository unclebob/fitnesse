package fitnesse.wikitext.parser;

import util.Maybe;

public class Literal extends SymbolType implements Rule {
    public static final Literal symbolType = new Literal();
    
    public Literal() {
        super("Literal");
        wikiMatcher(new Matcher().string("!-"));
        wikiRule(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        SymbolType type = current.getType();
        String literal = parser.parseLiteral(current.closeType());
        if (parser.atEnd())  return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(type, literal));
    }
}
