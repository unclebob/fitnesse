package fitnesse.wikitext.widgets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Widget for date (and optionally time) values.
 * <p/>
 * Usages:
 * <table>
 * <tr>
 * <td><code>!today</code></td>
 * <td>current date formatted with default date format {@link #dateFormat}</td>
 * </tr>
 * <tr>
 * <td><code>!today -t</code></td>
 * <td>current date and time formatted with {@link #dateFormatWithTime}</td>
 * </tr>
 * <tr>
 * <td><code>!today -xml</code></td>
 * <td>current date and time formatted for XML with {@link #xmlDateFormat}</td>
 * </tr>
 * <tr>
 * <td><code>!today (&lt;format string&gt;)</code></td>
 * <td>date/time formatted with the given format string</td>
 * </tr>
 * <tr><td colspan="2">All of these usages can be combined with a day offset, e.g.</td></tr>
 * <tr>
 * <td><code>!today +2</code></td>
 * <td>current date plus 2 days formatted with default date format {@link #dateFormat}</td>
 * </tr>
 * <tr>
 * <td><code>!today (dd.MM.yyyy) -3</code></td>
 * <td>current date minus 3 days formatted with the given format string</td>
 * </tr>
 * </table>
 */
public class TodayWidget extends ParentWidget {
  /**
   * Pattern string.
   */
  public static final String REGEXP = "!today(?: +(?:-t|-xml|\\(.*\\)))?( +((\\-|\\+)\\d+))?";

  /**
   * Compiled pattern.
   */
  public static final Pattern PATTERN = Pattern.compile("!today( +(?:(-t)|(-xml)|\\((.*)\\)))?( +((\\-|\\+)\\d+))?");

  private boolean withTime = false;

  private boolean xml = false;

  private SimpleDateFormat explicitDateFormat = null;

  private int dayDiff;

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");

  private static SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("dd MMM, yyyy HH:mm");

  private static SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * @see ParentWidget
   */
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

  /**
   * @see fitnesse.wikitext.WikiWidget#render()
   */
  public String render() throws Exception {
    Calendar cal = GregorianCalendar.getInstance();
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

