// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.util;

import java.util.Date;
import java.util.TimeZone;

public abstract class Clock {
  static final SystemClock SYSTEM_CLOCK = new SystemClock();
  static Clock instance;
  static {
    restoreDefaultClock();
  }

  protected Clock() {
    this(false);
  }

  protected Clock(boolean setAsInstance) {
    if (setAsInstance) instance = this;
  }

  protected abstract long currentClockTimeInMillis() ;

  protected abstract TimeZone getTimeZone();

  protected Date currentClockDate() {
    return new Date(currentClockTimeInMillis());
  }

  public static long currentTimeInMillis() {
      return instance.currentClockTimeInMillis();
  }

  public static Date currentDate() {
    return instance.currentClockDate();
  }

  public static TimeZone currentTimeZone() {
    return instance.getTimeZone();
  }

  public static void restoreDefaultClock() {
    Clock.instance = SYSTEM_CLOCK;
  }

}

class SystemClock extends Clock {
  @Override
  protected long currentClockTimeInMillis() {
    return System.currentTimeMillis();
  }

  protected TimeZone getTimeZone() {
    return TimeZone.getDefault();
  }
}
