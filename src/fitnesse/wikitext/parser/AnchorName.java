package fitnesse.wikitext.parser;

import java.util.List;

public class AnchorName extends SymbolType implements Rule {

    public AnchorName() {
        super("AnchorName");
        wikiMatcher(new Matcher().string("!anchor"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("a").attribute("name", 0).body(-1, " ").inline());
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (tokens.isEmpty()) return Symbol.nothing;

        String anchor = tokens.get(1).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.nothing;

        current.add(tokens.get(1));
        return new Maybe<>(current);
    }
}
