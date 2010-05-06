package fitnesse.wikitext.parser;

import util.Maybe;

public class IncludeRule implements Rule {
    private static final String[] setUpSymbols = new String[] {"COLLAPSE_SETUP"};
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;
        
        next = parser.moveNext(1);
        String option = "";
        if (next.isType(SymbolType.Text) && next.getContent().startsWith("-")) {
            option = next.getContent();
            next = parser.moveNext(1);
            if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;
            next = parser.moveNext(1);
        }
        if (!next.isType(SymbolType.Text) && !next.isType(SymbolType.WikiWord)) return Symbol.nothing;

        current.add(option).add(next);

        Maybe<SourcePage> includedPage = parser.getPage().getPage().findIncludedPage(next.getContent());
        if (includedPage.isNothing()) {
            current.add(new Symbol(SymbolType.Meta).add(includedPage.because()));
        }
        else {
            ParsingPage included = option.equals("-setup") || option.equals("-teardown")
                    ? parser.getPage()
                    : parser.getPage().copyForNamedPage(includedPage.getValue());
            current.add("").add(Parser.make(
                            included,
                            includedPage.getValue().getContent())
                            .parse());
            if (option.equals("-setup")) current.evaluateVariables(setUpSymbols, parser.getVariableSource());
        }

        return new Maybe<Symbol>(current);
    }
}
