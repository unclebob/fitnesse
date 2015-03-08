package fitnesse.wikitext.parser;

public class See extends SymbolType implements Rule {
    public static final See symbolType = new See();

    public See() {
        super("See");
        wikiMatcher(new Matcher().startLineOrCell().string("!see").whitespace());
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("b").body(0, "See: ").inline());
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (next.isType(WikiWord.symbolType)) {
            return new Maybe<Symbol>(current.add(next));
        }
        if (next.isType(Alias.symbolType)) {
            Maybe<Symbol> maybe = next.getType().getWikiRule().parse(next, parser);
            if (maybe!=null && maybe.getValue()!=null)
              return new Maybe<Symbol>(current.add(maybe.getValue()));
        }
        return Symbol.nothing;
    }
}
