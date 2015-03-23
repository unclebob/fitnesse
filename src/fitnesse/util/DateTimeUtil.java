package fitnesse.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
  private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

  public static long getTimeFromString(String time) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    try {
      Date date = format.parse(time);
      return date.getTime();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Date getDateFromString(String dateString) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    try {
      Date date = format.parse(dateString);
      return date;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String formatDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    return format.format(date);
  }

  public static boolean datesNullOrEqual(Date d1, Date d2) {
    return (d1 == null && d2 == null) || (d1 != null && d2 != null && d1.equals(d2));
  }
}
