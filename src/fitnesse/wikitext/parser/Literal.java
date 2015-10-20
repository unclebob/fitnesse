package fitnesse.wikitext.parser;

public class Literal extends SymbolType implements Rule {
    public static final Literal symbolType = new Literal();
    
    public Literal() {
        super("Literal", CloseLiteral);
        wikiMatcher(new Matcher().string("!-"));
        wikiRule(this);
    }
    
    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        SymbolType type = current.getType();
        int offset = parser.getOffset();
        String literal = parser.parseLiteral(closeType());
        if (parser.atEnd())  return Symbol.nothing;
        return new Maybe<Symbol>(new Symbol(type, literal, offset));
    }
}
