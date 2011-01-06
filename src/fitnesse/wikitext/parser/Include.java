package fitnesse.wikitext.parser;

import util.Maybe;

public class Include extends SymbolType implements Rule, Translation {
    private static final String[] setUpSymbols = new String[] {"COLLAPSE_SETUP"};

    public Include() {
        super("Include");
        wikiMatcher(new Matcher().startLineOrCell().string("!include"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
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
        if (!next.isType(SymbolType.Text) && !next.isType(WikiWord.symbolType)) return Symbol.nothing;

        current.add(option).add(next);

        Maybe<SourcePage> includedPage = parser.getPage().getNamedPage().findIncludedPage(next.getContent());
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

    public String toTarget(Translator translator, Symbol symbol) {
        if (symbol.getChildren().size() < 4) {
            return translator.translate(symbol.childAt(2));
        }
        String option = symbol.childAt(0).getContent();
        if (option.equals("-seamless")) {
            return translator.translate(symbol.childAt(3));
        }
        else {
            String collapseState = stateForOption(option, symbol);
            String title = "Included page: "
                    + translator.translate(symbol.childAt(1))
                    + " "
                    + new WikiWordBuilder(translator.getPage(), symbol.childAt(1).getContent(), "(edit)")
                        .buildLink("?edit&amp;redirectToReferer=true&amp;redirectAction=", symbol.childAt(1).getContent());
            return Collapsible.generateHtml(collapseState, title, translator.translate(symbol.childAt(3)));
        }
    }

    private String stateForOption(String option, Symbol symbol) {
        return ((option.equals("-setup") || option.equals("-teardown")) && symbol.getVariable("COLLAPSE_SETUP", "true").equals("true"))
                || option.equals("-c")
                ? Collapsible.Closed
                : Collapsible.Open;
    }
}
