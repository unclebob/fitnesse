package fitnesse.wikitext.widgets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodayWidget extends ParentWidget {
  public static final String REGEXP = "!today(?: +(?:-t|-xml|\\(.*\\)))?( +((\\-|\\+)\\d+))?";
  public static final Pattern PATTERN = Pattern.compile("!today( +(?:(-t)|(-xml)|\\((.*)\\)))?( +((\\-|\\+)\\d+))?");

  private boolean withTime = false;
  private boolean xml = false;
  private SimpleDateFormat explicitDateFormat = null;
  private int dayDiff;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
  private SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("dd MMM, yyyy HH:mm");
  private SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  public static Calendar todayForTest = null;

  public TodayWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = PATTERN.matcher(text);
    if (!match.find()) {
      System.err.println("TodayWidget: match was not found, text = '" + text + "'");
    } else {
      withTime = (match.group(2) != null);
      xml = (match.group(3) != null);
      String formatString = match.group(4);
      if (formatString != null) {
        explicitDateFormat = new SimpleDateFormat(formatString);
      }

      String s = match.group(6);
      if (s != null) {
        if (s.startsWith("+")) {
          s = s.substring(1);
        }
        dayDiff = Integer.parseInt(s);
      }
    }
  }

  public String render() throws Exception {
    Calendar cal = todayForTest != null ? todayForTest : GregorianCalendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, dayDiff);

    Date date = cal.getTime();

    String result;
    if (withTime) {
      result = dateFormatWithTime.format(date);
    } else if (xml) {
      result = xmlDateFormat.format(date);
    } else {
      if (explicitDateFormat != null) {
        result = explicitDateFormat.format(date);
      } else {
        result = dateFormat.format(date);
      }
    }
    return result;
  }
}

