package fitnesse.wikitext.shared;

import fitnesse.html.HtmlTag;
import fitnesse.util.Clock;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wikitext.SourcePage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastModifiedHtml {
  public static String write(SourcePage page) {
    String user = page.getProperty(WikiPageProperty.LAST_MODIFYING_USER);
    String date = page.getProperty(WikiPageProperty.LAST_MODIFIED);
    return HtmlTag.name("span").attribute("class", "meta").body(
      "Last modified " +
        (!user.isEmpty() ? "by " + user : "anonymously") +
        " on " + formatDate(date)).htmlInline();
  }

  private static String formatDate(String dateString) {
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
