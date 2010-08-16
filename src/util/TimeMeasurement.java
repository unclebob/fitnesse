// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.util.Calendar;
import java.util.Date;

public class TimeMeasurement {

  private final Clock clock;
  private Long startedAt;
  private Long stoppedAt;
  
  public TimeMeasurement() {
    this(Clock.instance);
  }

  TimeMeasurement(Clock measuringClock) {
    this.clock = measuringClock;
  }

  public TimeMeasurement start() {
    this.startedAt = currentClockTimeInMillis();
    this.stoppedAt = null;
    return this;
  }

  private long currentClockTimeInMillis() {
    return clock.currentClockTimeInMillis();
  }

  public long startedAt() {
    if (isStarted()) {
      return startedAt;
    }
    throw new IllegalStateException("Call start() before getting startedAt()!");
  }

  private boolean isStarted() {
    return startedAt != null;
  }

  public long elapsed() {
    if (isStopped()) {
      return stoppedAt() - startedAt();
    }
    return currentClockTimeInMillis() - startedAt();
  }

  private boolean isStopped() {
    return stoppedAt != null;
  }

  public TimeMeasurement stop() {
    if (!isStopped()) {
      stoppedAt = currentClockTimeInMillis();
    }
    return this;
  }

  public long stoppedAt() {
    if (isStopped()) {
      return stoppedAt;
    }
    throw new IllegalStateException("Call stop() before getting stoppedAt()!");
  }

  public Date startedAtDate() {
    return new Date(startedAt());
  }

  public Date stoppedAtDate() {
    return new Date(stoppedAt());
  }

  public double elapsedSeconds() {
    return elapsed() / 1000d;
  }

}
