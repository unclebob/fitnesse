package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class Path extends SymbolType implements Rule, PathsCollector {
    public static final Path symbolType = new Path();

    public Path() {
        super("Path");
        wikiMatcher(new Matcher().startLine().string("!path"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("span").body(0, "classpath: ").attribute("class", "meta").inline());
    }

    public Collection<String> collectPaths(Translator translator,  Symbol symbol) {
        List<String> paths = new ArrayList<String>();
        paths.add(translator.translate(symbol.childAt(0)));
        return paths;
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(parser.parseToEnds(0, SymbolProvider.pathRuleProvider, new SymbolType[] {SymbolType.Newline})));
    }
}
