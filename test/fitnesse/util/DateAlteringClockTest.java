package fitnesse.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DateAlteringClockTest {

  private SimpleDateFormat ymdDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  
  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }
  
  @Test
  public void currentClockTimeInMillisShouldBeRebasedToConstructorArg() throws Exception {
    Date startOfTheDecade = ymdDateFormat.parse("2010-01-01");
    new DateAlteringClock(startOfTheDecade);
    assertThat(ymdDateFormat.format(new Date(Clock.currentTimeInMillis())), is("2010-01-01"));
  }
  
  @Test
  public void currentClockTimeInMillisShouldTickOnFromZero() throws Exception {
    Date endOfTheDecade = ymdDateFormat.parse("2010-12-31");
    new DateAlteringClock(endOfTheDecade);
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
    new DateAlteringClock(systemClock.currentClockDate()).freeze();
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
    new DateAlteringClock(startOfTheCentury).advanceMillisOnEachQuery();
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 1));
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 2));
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 3));
  }

  @Test
  public void shouldBeAbleToDefineElapsedTime() throws Exception {
    Date startOfTheCentury = ymdDateFormat.parse("2000-01-01");
    new DateAlteringClock(startOfTheCentury).freeze().elapse(39);
    assertThat(Clock.currentTimeInMillis(), is(startOfTheCentury.getTime() + 39));
  }


}
