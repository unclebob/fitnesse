package fitnesse.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

public class DateTimeUtil {
  private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
  private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";

  public static long getTimeFromString(String time) throws ParseException {
    return getDateFromString(time).getTime();
  }

  public static Date getDateFromString(String dateString) throws ParseException {
    return DateUtils.parseDateStrictly(dateString, new String[]{ ISO_DATE_FORMAT, DATE_FORMAT });
  }

  public static String formatDate(Date date) {
    return DateFormatUtils.format(date, ISO_DATE_FORMAT, TimeZone.getDefault(), Locale.US);
  }

  public static boolean datesNullOrEqual(Date d1, Date d2) {
    return (d1 == null && d2 == null) || (d1 != null && d2 != null && d1.equals(d2));
  }
}
