package fitnesse.wikitext.parser;

import util.Maybe;
import util.SystemTimeKeeper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Today extends SymbolType implements Rule, Translation {
    private static final String Format = "Format";
    private static final String Increment = "Increment";

    public Today() {
        super("Today");
        wikiMatcher(new Matcher().string("!today"));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (lookAhead.size() != 0 ) {
            String option = lookAhead.get(1).getContent();
            if (isDateFormatOption(option)) {
                current.putProperty(Today.Format, option);
                parser.moveNext(2);
            }
        }
        else {
            lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.OpenParenthesis});
            if (lookAhead.size() != 0) {
                parser.moveNext(2);
                String format = parser.parseToAsString(SymbolType.CloseParenthesis);
                if (parser.atEnd())  return Symbol.nothing;
                current.putProperty(Format, format);
            }
        }
        lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Delta});
        if (lookAhead.size() != 0) {
            String increment = lookAhead.get(1).getContent();
            current.putProperty(Increment, increment);
            parser.moveNext(2);
        }
        return new Maybe<Symbol>(current);
    }

    private boolean isDateFormatOption(String option) {
        return option.equals("-t")
                || option.equals("-xml");
    }
    public String toTarget(Translator translator, Symbol symbol) {
        String increment = symbol.getProperty(Today.Increment);
        int incrementDays =
                increment.startsWith("+") ? Integer.parseInt(increment.substring(1)) :
                increment.startsWith("-") ? - Integer.parseInt(increment.substring(1)) :
                0;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(SystemTimeKeeper.now());
        calendar.add(Calendar.DAY_OF_MONTH, incrementDays);
        return new SimpleDateFormat(
                makeFormat(symbol.getProperty(Today.Format)))
                        .format(calendar.getTime());
    }

    private String makeFormat(String format) {
        return
            format.equals("-t") ? "dd MMM, yyyy HH:mm" :
            format.equals("-xml") ? "yyyy-MM-dd'T'HH:mm:ss" :
            format.length() == 0 ? "dd MMM, yyyy" :
                format;
    }
}
