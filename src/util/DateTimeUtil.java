package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
  private static String dateFormatString = "MM/dd/yyyy HH:mm:ss";

  public static long getTimeFromString(String time) {
    SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
    try {
      Date date = format.parse(time);
      return date.getTime();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Date getDateFromString(String dateString) {
    SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
    try {
      Date date = format.parse(dateString);
      return date;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String formatDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
    return format.format(date);
  }

  public static boolean datesNullOrEqual(Date d1, Date d2) {
    return (d1 == null && d2 == null) || (d1 != null && d2 != null && d1.equals(d2));
  }
}
