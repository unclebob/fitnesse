package fitnesse.wikitext.parser;

import util.Maybe;

public class IncludeRule implements Rule {
    private static final String[] setUpSymbols = new String[] {"COLLAPSE_SETUP"};
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol include = scanner.getCurrent();

        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
        
        scanner.moveNext();
        String option = "";
        if (scanner.isType(SymbolType.Text) && scanner.getCurrentContent().startsWith("-")) {
            option = scanner.getCurrentContent();
            scanner.moveNext();
            if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Text) && !scanner.isType(SymbolType.WikiWord)) return Symbol.Nothing;

        Symbol pageName = scanner.getCurrent();
        include.add(option).add(pageName);

        Maybe<SourcePage> includedPage = parser.getPage().getPage().findIncludedPage(pageName.getContent());
        if (includedPage.isNothing()) {
            include.add(new Symbol(SymbolType.Meta).add(includedPage.because()));
        }
        else {
            ParsingPage included = option.equals("-setup") || option.equals("-teardown")
                    ? parser.getPage()
                    : parser.getPage().copyForNamedPage(includedPage.getValue());
            include.add("").add(Parser.make(
                            included,
                            includedPage.getValue().getContent())
                            .parse());
            if (option.equals("-setup")) include.evaluateVariables(setUpSymbols, parser.getVariableSource());
        }

        return new Maybe<Symbol>(include);
    }
}
