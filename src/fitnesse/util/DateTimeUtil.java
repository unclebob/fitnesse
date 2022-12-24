package fitnesse.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

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
    return DateFormatUtils.format(date, ISO_DATE_FORMAT, Clock.currentTimeZone(), Locale.US);
  }

  public static boolean datesNullOrEqual(Date d1, Date d2) {
    return (d1 == null && d2 == null) || (d1 != null && d2 != null && d1.equals(d2));
  }
}
