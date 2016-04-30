package fitnesse.wikitext.parser;

public class Nesting extends SymbolType implements Rule, Translation{
    public static final Nesting symbolType = new Nesting();

    public Nesting() {
        super("Nesting");
        wikiMatcher(new Matcher().string("!("));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol nesting = parser.parseTo(SymbolType.CloseNesting, ParseSpecification.nestingPriority);
        if (!parser.getCurrent().isType(SymbolType.CloseNesting)) return Symbol.nothing;
        current.add(nesting);
        return new Maybe<>(current);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        return translator.translateTree(symbol) ;
    }
}
