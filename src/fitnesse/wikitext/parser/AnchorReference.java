package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;

import java.util.List;

public class AnchorReference extends SymbolType implements Rule, Translation {
    public AnchorReference() {
        super("AnchorReference");
        wikiMatcher(new Matcher().string(".#"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Text});
        if (tokens.isEmpty()) return Symbol.nothing;

        String anchor = tokens.get(0).getContent();
        if (!ScanString.isWord(anchor)) return Symbol.nothing;

        return new Maybe<>(current.add(tokens.get(0)));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String name = translator.translate(symbol.childAt(0));
        return HtmlUtil.makeLink("#" + name, ".#" + name).html();
    }
}
