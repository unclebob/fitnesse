package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Test;

public class TimeAlteringClockTest {

  private TimeAlteringClock clock;
  
  @After
  public void restoreDefaultClock() {
    clock.restoreDefaultClock();
  }
  
  @Test
  public void currentTimeOnConsecutiveCallsShouldBeConstructorParams() throws Exception {
    clock = new TimeAlteringClock(42,1221);
    assertThat(clock.currentClockTimeInMillis(), is(42L));
    assertThat(clock.currentClockTimeInMillis(), is(1221L));
  }

  @Test
  public void creationShouldReplaceStaticGlobalClock() throws Exception {
    clock = new TimeAlteringClock(64,1332);
    assertThat(Clock.currentTimeInMillis(), is(64L));
    assertThat(Clock.currentTimeInMillis(), is(1332L));
  }
  
  @Test
  public void restoreShouldRestoreStaticGlobalClock() throws Exception {
    clock = new TimeAlteringClock(0, 0);
    assertThat(Clock.currentTimeInMillis(), is(0L));
    clock.restoreDefaultClock();
    assertThat(Clock.currentTimeInMillis(), is(not(clock.currentClockTimeInMillis())));
  }
  
  @Test
  public void staticGlobalClockShouldAutomaticallyBeRestoredAfterConstructorParamsAreExhausted() throws Exception {
    clock = new TimeAlteringClock(0L);
    clock.currentClockTimeInMillis();
    assertThat(Clock.currentTimeInMillis(), is(not(0L)));
  }

  @Test
  public void constructorShouldAcceptDatesAsWellAsTimes() throws Exception {
    clock = new TimeAlteringClock(new Date(1), new Date(2));
    assertThat(Clock.currentTimeInMillis(), is(1L));
    assertThat(Clock.currentTimeInMillis(), is(2L));
  }
}
