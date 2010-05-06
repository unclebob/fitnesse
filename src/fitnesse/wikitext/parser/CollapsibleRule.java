package fitnesse.wikitext.parser;

import util.Maybe;

public class CollapsibleRule implements Rule {
    public static final String State = "State";
    public static final String Open = "Open";
    public static final String Closed = "Closed";
    public static final String Invisible = "Invisible";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String state = Open;
        Symbol next = parser.moveNext(1);
        if (next.getContent().equals(">")) {
            state = Closed;
            next = parser.moveNext(1);
        }
        else if (next.getContent().equals("<")) {
            state = Invisible;
            next = parser.moveNext(1);
        }
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        Symbol titleText = parser.parseToIgnoreFirst(SymbolType.Newline);
        if (parser.atEnd()) return Symbol.nothing;

        Symbol bodyText = parser.parseToIgnoreFirst(SymbolType.CloseCollapsible);
        if (parser.atEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(new Symbol(SymbolType.Collapsible)
                .putProperty(State, state)
                .add(titleText)
                .add(bodyText));
    }
}
