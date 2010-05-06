package fitnesse.wikitext.parser;

import fitnesse.wikitext.widgets.TOCWidget;
import util.Maybe;

public class ContentsRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
       Symbol body = parser.parseWithEnds(new SymbolType[] {SymbolType.Newline});
        for (Symbol option: body.getChildren()) {
            if (option.isType(SymbolType.Whitespace)) continue;
            if (!option.getContent().startsWith("-")) return Symbol.nothing;
            current.add(option);
        }

        current.evaluateVariables(
                new String[] {TOCWidget.HELP_TOC, TOCWidget.REGRACE_TOC, TOCWidget.PROPERTY_TOC, TOCWidget.FILTER_TOC},
                parser.getVariableSource());

        return new Maybe<Symbol>(current);
    }
}
