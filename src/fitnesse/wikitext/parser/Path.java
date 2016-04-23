package fitnesse.wikitext.parser;

import java.util.Collection;
import java.util.Arrays;

public class Path extends SymbolType implements Rule, PathsProvider {
    public static final Path symbolType = new Path();

    public Path() {
        super("Path");
        wikiMatcher(new Matcher().startLineOrCell().string("!path"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("span").body(0, "classpath: ").attribute("class", "meta").inline());
    }

    @Override
    public Collection<String> providePaths(Translator translator, Symbol symbol) {
        return Arrays.asList(translator.translate(symbol.childAt(0)));
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<>(current.add(parser.parseToEnds(0, SymbolProvider.pathRuleProvider, new SymbolType[]{SymbolType.Newline})));
    }
}
