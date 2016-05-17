package fitnesse.wikitext.parser;

import fitnesse.util.Clock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Today extends SymbolType implements Rule, Translation {
    private static final String Format = "Format";
    private static final String Increment = "Increment";
    private final int incrementUnit;

    public Today() {
        this("Today", "!today", Calendar.DAY_OF_MONTH);
    }

    protected Today(String symbolName, String symbolText, int unitForIncrement) {
        super(symbolName);
        incrementUnit = unitForIncrement;
        wikiMatcher(new Matcher().string(symbolText));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.DateFormatOption});
        if (!lookAhead.isEmpty()) {
            String option = lookAhead.get(1).getContent();
            if (isDateFormatOption(option)) {
                current.putProperty(Today.Format, option);
                parser.moveNext(2);
            }
        }
        else {
            lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.OpenParenthesis});
            if (!lookAhead.isEmpty()) {
                parser.moveNext(2);
                Maybe<String> format = parser.parseToAsString(SymbolType.CloseParenthesis);
                if (format.isNothing())  return Symbol.nothing;
                current.putProperty(Format, format.getValue());
            }
        }
        lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Delta});
        if (!lookAhead.isEmpty()) {
            String increment = lookAhead.get(1).getContent();
            current.putProperty(Increment, increment);
            parser.moveNext(2);
        }
        return new Maybe<>(current);
    }

    private boolean isDateFormatOption(String option) {
        return option.equals("-t")
                || option.equals("-xml");
    }
    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String increment = symbol.getProperty(Today.Increment);
        int incrementInt =
                increment.startsWith("+") ? Integer.parseInt(increment.substring(1)) :
                increment.startsWith("-") ? - Integer.parseInt(increment.substring(1)) :
                0;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(Clock.currentDate());
        addIncrement(calendar, incrementInt);
        return new SimpleDateFormat(
                makeFormat(symbol.getProperty(Today.Format)))
                        .format(calendar.getTime());
    }

    protected void addIncrement(GregorianCalendar calendar, int increment) {
        calendar.add(incrementUnit, increment);
    }

    private String makeFormat(String format) {
        return
            format.equals("-t") ? "dd MMM, yyyy HH:mm" :
            format.equals("-xml") ? "yyyy-MM-dd'T'HH:mm:ss" :
                    format.isEmpty() ? "dd MMM, yyyy" :
                format;
    }
}
