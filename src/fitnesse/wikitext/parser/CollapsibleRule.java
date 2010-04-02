package fitnesse.wikitext.parser;

import util.Maybe;

public class CollapsibleRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        String bodyClass = "collapsable";
        scanner.moveNext();
        if (scanner.getCurrentContent().equals(">")) {
            bodyClass = "hidden";
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        Symbol titleText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;

        Symbol bodyText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.CloseCollapsible);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Collapsible)
                .add(bodyClass)
                .add(titleText)
                .add(bodyText));
    }
}
