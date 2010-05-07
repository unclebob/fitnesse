package fitnesse.wikitext.translator;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.Symbol;
import util.Clock;
import util.SystemClock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastModifiedBuilder implements Translation {
    public String toTarget(Translator translator, Symbol symbol) {
        String user = translator.getPage().getProperty(PageData.LAST_MODIFYING_USER);
        String date = translator.getPage().getProperty(PageData.PropertyLAST_MODIFIED);
        return translator.formatMessage(
                "Last modified " +
                (user.length() > 0 ? "by " + user : "anonymously") +
                " on " + formatDate(date));
    }

    private String formatDate(String dateString) {
        Date date;
        if (dateString.length() == 0) date = SystemClock.now();
        else {
            try {
                date = WikiPageProperties.getTimeFormat().parse(dateString);
            }
            catch (ParseException e) {
                return dateString;
            }
        }
        return new SimpleDateFormat("MMM dd, yyyy").format(date) + " at " + new SimpleDateFormat("hh:mm:ss a").format(date);
    }
}
