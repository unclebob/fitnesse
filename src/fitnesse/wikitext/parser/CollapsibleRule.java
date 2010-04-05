package fitnesse.wikitext.parser;

import util.Maybe;

public class CollapsibleRule extends Rule {
    public static final String OpenState = "Open";
    public static final String ClosedState = "Closed";
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        String state = OpenState;
        scanner.moveNext();
        if (scanner.getCurrentContent().equals(">")) {
            state = ClosedState;
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        Symbol titleText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;

        Symbol bodyText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.CloseCollapsible);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Collapsible)
                .add(state)
                .add(titleText)
                .add(bodyText));
    }
}
