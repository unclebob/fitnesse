// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;

public class ClockTest {

  @After
  public void restoreSystemClock() {
    Clock.instance = new SystemClock();
  }
  
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
  public void staticTimeMethodShouldDelegateToInstance() throws Exception {
    @SuppressWarnings("serial")
    Clock constantTimeClock = new Clock(true) {
      @Override
      public long currentClockTimeInMillis() {
        return 1;
      }
    };
    assertThat(Clock.currentTimeInMillis(), is(1L));
  }
  
  @Test
  public void currentClockDateShouldDelegateToCurrentTimeInMillis() throws Exception {
    @SuppressWarnings("serial")
    Clock constantTimeClock = new Clock() {
      @Override
      public long currentClockTimeInMillis() {
        return 2;
      }
    };
    assertThat(constantTimeClock.currentClockDate().getTime(), is(2L));
  }

  @Test
  public void staticDateMethodShouldDelegateToInstance() throws Exception {
    @SuppressWarnings("serial")
    Clock constantTimeClock = new Clock(true) {
      @Override
      public long currentClockTimeInMillis() {
        return 3;
      }
    };
    assertThat(Clock.currentDate().getTime(), is(3L));
  }
  
  @Test
  public void globalClockShouldDelegateToInstance() throws Exception {
    @SuppressWarnings("serial")
    Clock constantTimeClock = new Clock(true) {
      @Override
      public long currentClockTimeInMillis() {
        return 4;
      }
    };
    assertThat(new GlobalClock().currentClockTimeInMillis(), is(4L));
  }
}
