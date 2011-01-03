package fitnesse.wikitext.parser;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperties;
import util.SystemTimeKeeper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastModified extends SymbolType implements Translation {
    public LastModified() {
        super("LastModified");
        wikiMatcher(new Matcher().string("!lastmodified"));
        htmlTranslation(this);
    }
    
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
        if (dateString.length() == 0) date = SystemTimeKeeper.now();
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
