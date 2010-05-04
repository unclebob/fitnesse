package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.TodayRule;
import util.SystemClock;

import java.text.SimpleDateFormat;

public class TodayBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        return new SimpleDateFormat(
                makeFormat(symbol.getProperty(TodayRule.Format)))
                        .format(SystemClock.now());
    }

    private String makeFormat(String format) {
        return
            format.equals("-t") ? "dd MMM, yyyy HH:mm" :
            format.equals("-xml") ? "yyyy-MM-dd'T'HH:mm:ss" :
                "dd MMM, yyyy";
    }
}
