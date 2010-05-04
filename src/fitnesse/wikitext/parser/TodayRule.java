package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class TodayRule implements Rule {
    public static final String Format = "-t";

    public Maybe<Symbol> parse(Parser parser) {
        Symbol current = parser.getCurrent();
        List<Symbol> lookAhead = parser.peek(2);
        if (lookAhead.get(0).getType() == SymbolType.Whitespace
                && lookAhead.get(1).getType() == SymbolType.Text) {
            if (lookAhead.get(1).getContent().equals("-t")
                    || lookAhead.get(1).getContent().equals("-xml")) {
                current.putProperty(TodayRule.Format, lookAhead.get(1).getContent());
            }
            parser.getScanner().moveNext();
            parser.getScanner().moveNext();
        }
        return new Maybe<Symbol>(current);
    }
}
