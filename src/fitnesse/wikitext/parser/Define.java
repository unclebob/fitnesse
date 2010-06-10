package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;
import java.util.List;

public class Define extends SymbolType implements Rule, Translation {
    public Define() {
        super("Define");
        wikiMatcher(new Matcher().startLine().string("!define"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> tokens = parser.moveNext(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text, SymbolType.Whitespace});
        if (tokens.size() == 0) return Symbol.nothing;

        String name = tokens.get(1).getContent();
        if (!ScanString.isVariableName(name)) return Symbol.nothing;

        Symbol next = parser.moveNext(1);
        SymbolType close = next.closeType();
        if (close == SymbolType.Empty) return Symbol.nothing;

        String valueString = parser.parseToAsString(close);
        if (parser.atEnd()) return Symbol.nothing;
        parser.getPage().putVariable(name, valueString);

        return new Maybe<Symbol>(current
                .add(name)
                .add(valueString));
    }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag("span", "variable defined: "
                + translator.translate(symbol.childAt(0))
                + "="
                + translator.translate(symbol.childAt(1)));
        result.addAttribute("class", "meta");
        return result.html();
    }
}
