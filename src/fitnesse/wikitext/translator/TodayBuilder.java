package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.TodayRule;
import util.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TodayBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        String increment = symbol.getProperty(TodayRule.Increment);
        int incrementDays =
                increment.startsWith("+") ? Integer.parseInt(increment.substring(1)) :
                increment.startsWith("-") ? - Integer.parseInt(increment.substring(1)) :
                0;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(SystemClock.now());
        calendar.add(Calendar.DAY_OF_MONTH, incrementDays);
        return new SimpleDateFormat(
                makeFormat(symbol.getProperty(TodayRule.Format)))
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
