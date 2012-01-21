package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;
import java.util.List;

public class Define extends SymbolType implements Rule, Translation {
    public Define() {
        super("Define");
        wikiMatcher(new Matcher().startLineOrCell().string("!define"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        Maybe<String> name = parser.parseToAsString(SymbolType.Whitespace);
        if (name.isNothing()) return Symbol.nothing;
        String variableName = name.getValue();
        if (!ScanString.isVariableName(variableName)) return Symbol.nothing;

        Symbol next = parser.moveNext(1);
        SymbolType close = next.closeType();
        if (close == SymbolType.Empty) return Symbol.nothing;

        Maybe<String> valueString = parser.parseToAsString(close);
        if (valueString.isNothing()) return Symbol.nothing;
        String variableValue = valueString.getValue();
        parser.getPage().putVariable(variableName, variableValue);

        return new Maybe<Symbol>(current
                .add(variableName)
                .add(variableValue));
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
