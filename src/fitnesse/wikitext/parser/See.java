package fitnesse.wikitext.parser;

public class See extends SymbolType implements Rule {
    public static final See symbolType = new See();

    public See() {
        super("See");
        wikiMatcher(new Matcher().startLineOrCell().string("!see").whitespace());
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("b").body(0, "See: ").inline());
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (next.isType(WikiWord.symbolType)) {
            return new Maybe<>(current.add(next));
        }
        if (next.isType(Alias.symbolType)) {
            Maybe<Symbol> alias = next.getType().getWikiRule().parse(next, parser);
            if (!alias.isNothing())
              return new Maybe<>(current.add(alias.getValue()));
        }
        return Symbol.nothing;
    }
}
