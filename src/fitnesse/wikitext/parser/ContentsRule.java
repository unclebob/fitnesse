package fitnesse.wikitext.parser;

import fitnesse.wikitext.widgets.TOCWidget;
import util.Maybe;

public class ContentsRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol result = scanner.getCurrent();

        Symbol body = parser.parseWithEnds(new SymbolType[] {SymbolType.Newline});
        for (Symbol option: body.getChildren()) {
            if (option.getType() == SymbolType.Whitespace) continue;
            if (!option.getContent().startsWith("-")) return Symbol.Nothing;
            result.add(option);
        }

        result.evaluateVariables(
                new String[] {TOCWidget.HELP_TOC, TOCWidget.REGRACE_TOC, TOCWidget.PROPERTY_TOC, TOCWidget.FILTER_TOC},
                parser.getVariableSource());

        return new Maybe<Symbol>(result);
    }
}
