package fitnesse.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtil {
  private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
  private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

  public static long getTimeFromString(String time) {
    return getDateFromString(time).getTime();
  }

  public static Date getDateFromString(String dateString) {
    SimpleDateFormat format = new SimpleDateFormat(ISO_DATE_FORMAT);
    try {
      return format.parse(dateString);
    } catch (ParseException pe) {
      SimpleDateFormat fallbackFormat = new SimpleDateFormat(DATE_FORMAT);
      try {
        return fallbackFormat.parse(dateString);
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String formatDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat(ISO_DATE_FORMAT);
    return format.format(date);
  }

  public static boolean datesNullOrEqual(Date d1, Date d2) {
    return (d1 == null && d2 == null) || (d1 != null && d2 != null && d1.equals(d2));
  }
}
