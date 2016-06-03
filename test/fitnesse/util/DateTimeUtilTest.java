package fitnesse.util;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateTimeUtilTest {

  @Test
  public void canParseIsoDate() throws ParseException {
    Date date = new Date();
    String formatted = DateTimeUtil.formatDate(date);
    Date newDate = DateTimeUtil.getDateFromString(formatted);
    // Chop off milliseconds, since they might differ.
    assertEquals(date.getTime() / 1000, newDate.getTime() / 1000);
  }

  @Test
  public void canParseISO8601DateFormat() throws ParseException {
    // If not, it will throw runtime exception
    String dateString = "2015-10-12T18:00:00+00:00";
    DateTimeUtil.getDateFromString(dateString);
  }

  @Test
  public void canParseUSDateFormat() throws ParseException {
    // If not, it will throw runtime exception
    String dateString = "12/31/1969 18:00:00";
    DateTimeUtil.getDateFromString(dateString);
  }

  @Test(expected = ParseException.class)
  public void canNotParseInvalidDateFormat() throws ParseException {
    String dateString = "1-1/69 18:00:00";
    DateTimeUtil.getDateFromString(dateString);
  }

  @Test
  public void dateIsFormattedAsIso8601() {
    Pattern isoPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[+-]\\d{2}:\\d{2})");
    String formatted = DateTimeUtil.formatDate(new Date());
    assertTrue(String.format("String '%s' does not match pattern '%s'", formatted, isoPattern.pattern()), isoPattern.matcher(formatted).find());

  }
}
