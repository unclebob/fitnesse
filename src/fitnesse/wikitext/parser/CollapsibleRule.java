package fitnesse.wikitext.parser;

import util.Maybe;

public class CollapsibleRule extends Rule {
    public static final String OpenState = "Open";
    public static final String ClosedState = "Closed";
    public static final String InvisibleState = "Invisible";

    @Override
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
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        Symbol titleText = Parser.makeIgnoreFirst(getPage(), scanner, SymbolType.Newline).parse();
        if (scanner.isEnd()) return Symbol.Nothing;

        Symbol bodyText = Parser.makeIgnoreFirst(getPage(), scanner, SymbolType.CloseCollapsible).parse();
        if (scanner.isEnd()) return Symbol.Nothing;

        bodyText.removeLastChild();
        return new Maybe<Symbol>(new Symbol(SymbolType.Collapsible)
                .add(state)
                .add(titleText)
                .add(bodyText));
    }
}
