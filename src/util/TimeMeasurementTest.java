// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeMeasurementTest {
  private Clock mockedClock;
  
  @Before
  public void mockClock() {
    mockedClock = mock(SystemClock.class);
  }
  
  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }
  
  @Test
  public void timeMeasurementShouldStartAtClockTime() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-2L);
    measurement.start();
    assertThat(measurement.startedAt(), is(-2L));
  }
  
  @Test
  public void elapsedTimeShouldReferenceClockTimeWhenNotStopped() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-3L, -1L);
    measurement.start();
    assertThat(measurement.elapsed(), is(2L));
  }
  
  @Test
  public void stopShouldReferenceClockTime() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-7L, -4L);
    measurement.start();
    measurement.stop();
    assertThat(measurement.stoppedAt(), is(-4L));
  }
  
  @Test
  public void stopShouldFreezeElapsedTime() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-9L, -8L, -6L);
    measurement.start();
    measurement.stop();
    assertThat(measurement.elapsed(), is(1L));
    assertThat(measurement.elapsed(), is(1L));
  }

  @Test(expected=IllegalStateException.class)
  public void startedAtBeforeStartShouldThrowIllegalStateException() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    measurement.startedAt();
  }

  @Test(expected=IllegalStateException.class)
  public void stoppedAtBeforeStopShouldThrowIllegalStateException() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    measurement.start();
    measurement.stoppedAt();
  }

  @Test
  public void callingStopMultipleTimesShouldHaveNoEffect() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-13L, -12L, -11L);
    measurement.start();
    measurement.stop();
    measurement.stop();
    assertThat(measurement.elapsed(), is(1L));
  }
  
  @Test
  public void stopStartShouldResetTheStartedAndStoppedAtTimes() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-17L, -16L, -15L, -14L);
    measurement.start();
    assertThat(measurement.startedAt(), is(-17L));
    measurement.stop();
    assertThat(measurement.stoppedAt(), is(-16L));
    measurement.start();
    assertThat(measurement.startedAt(), is(-15L));
    measurement.stop();
    assertThat(measurement.stoppedAt(), is(-14L));
  }

  @Test
  public void stopStartShouldAffectElapsedTimeCalculations() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-25L,-24L,-23L,-21L,-21L,-19L,-18L);
    measurement.start();
    measurement.stop();
    assertThat(measurement.elapsed(), is(1L));
    measurement.start();
    measurement.stop();
    assertThat(measurement.elapsed(), is(2L));
    measurement.start();
    assertThat(measurement.elapsed(), is(2L));
    measurement.stop();
    assertThat(measurement.elapsed(), is(3L));
  }
  
  @Test
  public void callingStartMultipleTimesShouldResetStartedAtAndElapsed() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement(mockedClock);
    when(mockedClock.currentClockTimeInMillis()).thenReturn(-30L,-29L,-29L,-28L,-27L);
    measurement.start();
    assertThat(measurement.startedAt(), is(-30L));
    measurement.start();
    assertThat(measurement.startedAt(), is(-29L));
    assertThat(measurement.elapsed(), is(0L));
    measurement.start();
    assertThat(measurement.startedAt(), is(-28L));
    assertThat(measurement.elapsed(), is(1L));
  }
  
  @Test
  public void startShouldReturnSelfForCallChaining() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    assertThat(measurement.start(), is(sameInstance(measurement)));
  }
  
  @Test
  public void stopShouldReturnSelfForCallChaining() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    measurement.start();
    assertThat(measurement.stop(), is(sameInstance(measurement)));
  }
  
  @Test
  public void startedAtDateShouldBeDateRepresentationOfStartedAt() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    assertThat(measurement.start().startedAtDate().getTime(), is(measurement.startedAt()));
  }
  
  @Test
  public void stoppedAtDateShouldBeDateRepresentationOfStoppedAt() throws Exception {
    TimeMeasurement measurement = new TimeMeasurement();
    measurement.start();
    assertThat(measurement.stop().stoppedAtDate().getTime(), is(measurement.stoppedAt()));
  }
  
  @Test
  public void elapsedSecondsShouldBeDoubleRepresentationOfElapsed() throws Exception {
    assertThat(timeMeasurementWithElapsedMillis(1).elapsedSeconds(), is(0.001d));
    assertThat(timeMeasurementWithElapsedMillis(1000).elapsedSeconds(), is(1.0d));
    assertThat(timeMeasurementWithElapsedMillis(2345).elapsedSeconds(), is(2.345d));
    assertThat(timeMeasurementWithElapsedMillis(0).elapsedSeconds(), is(0d));
  }

  private TimeMeasurement timeMeasurementWithElapsedMillis(final long millis) {
    return new TimeMeasurement() {
      @Override
      public long elapsed() {
        return millis;
      }
    };
  }
  
  @Test
  public void alteringGlobalClockShouldNotAffectExistingTimeMeasurement() throws Exception {
    TimeMeasurement timeMeasurement = new TimeMeasurement();
    DateAlteringClock globalClock = new DateAlteringClock(Clock.currentDate()).freeze();
    TimeMeasurement frozentTimeMeasurement = new TimeMeasurement().start();
    timeMeasurement.start();
    SystemClock systemClock = new SystemClock();
    long before = 0, after = 0;
    while (before == after) {
      after = systemClock.currentClockTimeInMillis();
      if (before == 0) {
        before = after;
      }
    }
    assertThat(frozentTimeMeasurement.elapsed(), is(0L));
    assertThat(timeMeasurement.elapsed(), is(not(0L)));
  }
}
