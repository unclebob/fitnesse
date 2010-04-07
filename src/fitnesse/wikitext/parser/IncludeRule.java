package fitnesse.wikitext.parser;

import util.Maybe;

public class IncludeRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
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
        scanner.moveNext();
        if (!scanner.isEnd() && !scanner.isType(SymbolType.Newline) ) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Include).add(option).add(pageName));
    }
}
