package fitnesse.wikitext.parser;

import util.Maybe;

public class CollapsibleRule implements Rule {
    public static final String OpenState = "Open";
    public static final String ClosedState = "Closed";
    public static final String InvisibleState = "Invisible";

    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        String state = OpenState;
        scanner.moveNext();
        if (scanner.getCurrentContent().equals(">")) {
            state = ClosedState;
            scanner.moveNext();
        }
        else if (scanner.getCurrentContent().equals("<")) {
            state = InvisibleState;
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.nothing;

        Symbol titleText = parser.parseToIgnoreFirst(SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.nothing;

        Symbol bodyText = parser.parseToIgnoreFirst(SymbolType.CloseCollapsible);
        if (scanner.isEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Collapsible)
                .add(state)
                .add(titleText)
                .add(bodyText));
    }
}
