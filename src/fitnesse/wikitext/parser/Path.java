package fitnesse.wikitext.parser;

import util.Maybe;

public class Path extends SymbolType implements Rule {
    public static final Path symbolType = new Path();
    
    public Path() {
        super("Path");
        wikiMatcher(new Matcher().startLine().string("!path"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("span").body(0, "classpath: ").attribute("class", "meta").inline());
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(parser.parseWithEnds(SymbolProvider.pathRuleProvider, new SymbolType[] {SymbolType.Newline})));
    }
}
