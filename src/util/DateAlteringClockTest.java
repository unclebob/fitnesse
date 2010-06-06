package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Test;

public class DateAlteringClockTest {

  private DateAlteringClock clock;
  private SimpleDateFormat ymdDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  
  @After
  public void restoreDefaultClock() {
    clock.restoreDefaultClock();
  }
  
  @Test
  public void currentClockTimeInMillisShouldBeRebasedToConstructorArg() throws Exception {
    Date startOfTheDecade = ymdDateFormat.parse("2010-01-01");
    clock = new DateAlteringClock(startOfTheDecade);
    assertThat(ymdDateFormat.format(new Date(Clock.currentTimeInMillis())), is("2010-01-01"));
  }
  
  @Test
  public void restoringDefaultClockShouldUndoRebasing() throws Exception {
    Date yesterday = ymdDateFormat.parse("2010-06-05");
    clock = new DateAlteringClock(yesterday);
    clock.restoreDefaultClock();
    assertThat(ymdDateFormat.format(new Date(Clock.currentTimeInMillis())), is(not("2010-06-05")));
  }

  @Test
  public void currentClockTimeInMillisShouldTickOnAsNormal() throws Exception {
    Date endOfTheDecade = ymdDateFormat.parse("2010-12-31");
    clock = new DateAlteringClock(endOfTheDecade);
    long before = 0, after = 0;
    while (after == before) {
      after = Clock.currentTimeInMillis();
      if (before == 0) {
        before = after;
      }
    }
  }
  
}
