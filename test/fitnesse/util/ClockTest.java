// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.util;

import org.junit.After;
import org.junit.Test;

import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClockTest {

  @After
  public void restoreSystemClock() {
    Clock.restoreDefaultClock();
  }

  @SuppressWarnings("static-access")
  @Test
  public void systemClockTimeInMillisShouldIncreaseAsTimeFlies() throws Exception {
    Clock clock = new SystemClock();
    long currentTime = 0, priorTime = 0;
    while (currentTime == priorTime) {
      currentTime = clock.currentTimeInMillis();
      if (priorTime == 0) {
        priorTime = currentTime;
      }
    }
  }

  @Test
  public void defaultTimeZoneReturned() {
    Clock clock = new SystemClock();
    assertThat(clock.getTimeZone(), is(TimeZone.getDefault()));
  }

  @Test
  public void staticTimeMethodShouldDelegateToInstance() throws Exception {
    newConstantTimeClock(1, true);
    assertThat(Clock.currentTimeInMillis(), is(1L));
  }

  private Clock newConstantTimeClock(final long theConstantTime, final boolean overrideGlobalClock) {
    return new Clock(overrideGlobalClock) {
      @Override
      public long currentClockTimeInMillis() {
        return theConstantTime;
      }

      @Override
      protected TimeZone getTimeZone() {
        return TimeZone.getTimeZone("Antarctica/Casey");
      }
    };
  }

  @Test
  public void dateMethodShouldDelegateToCurrentTimeInMillis() throws Exception {
    Clock constantTimeClock = newConstantTimeClock(2, false);
    assertThat(constantTimeClock.currentClockDate().getTime(), is(2L));
  }

  @Test
  public void staticDateMethodShouldDelegateToInstance() throws Exception {
    newConstantTimeClock(3, true);
    assertThat(Clock.currentDate().getTime(), is(3L));
  }

  @Test
  public void staticTimeZoneMethodShouldDelegateToInstance() throws Exception {
    newConstantTimeClock(3, true);
    assertThat(Clock.currentTimeZone(), is(TimeZone.getTimeZone("Antarctica/Casey")));
  }

  @Test
  public void booleanConstructorArgShouldDetermineWhetherToReplaceGlobalInstance() throws Exception {
    Clock constantTimeClock = newConstantTimeClock(4, false);
    assertThat(Clock.instance, is(not(constantTimeClock)));
    constantTimeClock = newConstantTimeClock(5, true);
    assertThat(Clock.instance, is(constantTimeClock));
  }

  @Test
  public void shouldBeAbleToRestoreDefaultClock() throws Exception {
    long before = Clock.currentTimeInMillis();
    newConstantTimeClock(0, true);
    Clock.restoreDefaultClock();
    assertThat(Clock.currentTimeInMillis(), is(not(0L)));
    assertTrue(Clock.currentTimeInMillis() - before < 1000);
  }
}
