package fitnesse.wikitext.parser;

import util.Maybe;

public class See extends SymbolType implements Rule {
    public static final See symbolType = new See();

    public See() {
        super("See");
        wikiMatcher(new Matcher().string("!see").whitespace());
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("b").body(0, "See: ").inline());
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(WikiWord.symbolType)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(next));
    }
}
