package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class TodayRule implements Rule {
    public static final String Format = "Format";
    public static final String Increment = "Increment";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (lookAhead.size() != 0 ) {
            String option = lookAhead.get(1).getContent();
            if (isDateFormatOption(option)) {
                current.putProperty(TodayRule.Format, option);
                parser.moveNext(2);
            }
        }
        else {
            lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.OpenParenthesis, SymbolType.Text, SymbolType.CloseParenthesis});
            if (lookAhead.size() != 0) {
                current.putProperty(Format, lookAhead.get(2).getContent());
                parser.moveNext(4);
            }
        }
        lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (lookAhead.size() != 0) {
            String increment = lookAhead.get(1).getContent();
            if ((increment.startsWith("+") || increment.startsWith("-"))
                    && ScanString.isDigits(increment.substring(1))) {
                current.putProperty(Increment, increment);
                parser.moveNext(2);
            }
        }
        return new Maybe<Symbol>(current);
    }

    private boolean isDateFormatOption(String option) {
        return option.equals("-t")
                || option.equals("-xml");
    }
}
