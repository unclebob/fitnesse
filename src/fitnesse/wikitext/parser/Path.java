package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.ToHtml;

import java.util.Collection;
import java.util.Collections;

public class Path extends SymbolType implements Rule, PathsProvider {
    public static final Path symbolType = new Path();

    public Path() {
        super("Path");
        wikiMatcher(new Matcher().startLineOrCell().string("!path"));
        wikiRule(this);
        htmlTranslation(Translate.with(ToHtml::path).child(0));
    }

    @Override
    public Collection<String> providePaths(Translator translator, Symbol symbol) {
        return Collections.singletonList(translator.translate(symbol.childAt(0)));
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<>(current.add(parser.parseToEnds(0, SymbolProvider.pathRuleProvider, new SymbolType[]{SymbolType.Newline})));
    }
}
