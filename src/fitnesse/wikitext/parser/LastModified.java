package fitnesse.wikitext.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.util.Clock;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperty;

public class LastModified extends SymbolType implements Translation {
    public LastModified() {
        super("LastModified");
        wikiMatcher(new Matcher().string("!lastmodified"));
        htmlTranslation(this);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String user = translator.getPage().getProperty(PageData.LAST_MODIFYING_USER);
        String date = translator.getPage().getProperty(WikiPageProperty.LAST_MODIFIED);
        return translator.formatMessage(
                "Last modified " +
                (!user.isEmpty() ? "by " + user : "anonymously") +
                " on " + formatDate(date));
    }

    private String formatDate(String dateString) {
        Date date;
        if (dateString.isEmpty()) date = Clock.currentDate();
        else {
            try {
                date = WikiPageProperty.getTimeFormat().parse(dateString);
            }
            catch (ParseException e) {
                return dateString;
            }
        }
        return new SimpleDateFormat("MMM dd, yyyy").format(date) + " at " + new SimpleDateFormat("hh:mm:ss a").format(date);
    }
}
