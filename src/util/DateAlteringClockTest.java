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
    Clock.restoreDefaultClock();
  }
  
  @Test
  public void currentClockTimeInMillisShouldBeRebasedToConstructorArg() throws Exception {
    Date startOfTheDecade = ymdDateFormat.parse("2010-01-01");
    clock = new DateAlteringClock(startOfTheDecade);
    assertThat(ymdDateFormat.format(new Date(Clock.currentTimeInMillis())), is("2010-01-01"));
  }
  
  @Test
  public void currentClockTimeInMillisShouldTickOnFromZero() throws Exception {
    Date endOfTheDecade = ymdDateFormat.parse("2010-12-31");
    clock = new DateAlteringClock(endOfTheDecade);
    long before = 0, after = 0;
    while (after == before) {
      after = Clock.currentTimeInMillis();
      if (before == 0) {
        before = after;
      }
    }
    assertTrue(Clock.currentTimeInMillis() - endOfTheDecade.getTime() < 1000);
  }
  
  @Test
  public void shouldBeAbleToFreezeClockTime() throws Exception {
    SystemClock systemClock = new SystemClock();
    long before = 0, after = 0;
    clock = new DateAlteringClock(systemClock.currentClockDate()).freeze();
    long frozenTime = Clock.currentTimeInMillis(); 
    while (after == before) {
      after = systemClock.currentClockTimeInMillis();
      if (before == 0) {
        before = after;
      }
    }
    assertThat(Clock.currentTimeInMillis(), is(frozenTime));
  }  
  
  @Test
  public void shouldBeAbleToAdvanceClockTimeOnEachQuery() throws Exception {
    Date startOfTheCentury = ymdDateFormat.parse("2000-01-01");
    clock = new DateAlteringClock(startOfTheCentury).advanceMillisOnEachQuery();
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 1));
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 2));
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 3));
  }
  
}
