package fitnesse.wikitext.widgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TodayWidgetTest {
  @Before
  public void setup() {
    TodayWidget.todayForTest = new GregorianCalendar(1952, 11, 5, 1, 13, 23);  //GDTH unit date!!!  Eleven == Dec
  }

  @After
  public void teardown() {
    TodayWidget.todayForTest = null;
  }

  private boolean matches(String widget) {
    return Pattern.matches(TodayWidget.REGEXP, widget);
  }

  private void assertRenders(String widgetString, String result) throws Exception {
    TodayWidget widget = new TodayWidget(new MockWidgetRoot(), widgetString);
    Assert.assertEquals(result, widget.render());
  }

  @Test
  public void shouldMatch() throws Exception {
    assertTrue(matches("!today"));
    assertTrue(matches("!today -t"));
    assertTrue(matches("!today -xml"));
    assertTrue(matches("!today +3"));
    assertTrue(matches("!today -3"));
    assertTrue(matches("!today (MMM)"));
    assertTrue(matches("!today (MMM) +3"));
  }

  @Test
  public void shouldNotMatch() throws Exception {
    assertFalse(matches("!today -p"));
    assertFalse(matches("!today 33"));
    assertFalse(matches("!today x"));
  }

  @Test
  public void today() throws Exception {
    assertRenders("!today", "05 Dec, 1952");
  }

  @Test
  public void withTime() throws Exception {
    assertRenders("!today -t", "05 Dec, 1952 01:13");
  }

  @Test
  public void xml() throws Exception {
    assertRenders("!today -xml", "1952-12-05T01:13:23");
  }

  @Test
  public void addOneDay() throws Exception {
    assertRenders("!today +1", "06 Dec, 1952");
  }

  @Test
  public void subtractOneDay() throws Exception {
    assertRenders("!today -1", "04 Dec, 1952");    
  }

  @Test
  public void subtractOneWeek() throws Exception {
    assertRenders("!today -7", "28 Nov, 1952");
  }

  @Test
  public void addOneYear() throws Exception {
    assertRenders("!today +365", "05 Dec, 1953");
  }

  @Test
  public void format() throws Exception {
    assertRenders("!today (MMM)", "Dec");
  }

  @Test
  public void formatPlusOneDay() throws Exception {
    assertRenders("!today (ddMMM) +1", "06Dec");    
  }

}
