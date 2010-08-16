// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.util.Date;

public abstract class Clock {
  protected static final SystemClock SYSTEM_CLOCK = new SystemClock();
  protected static Clock instance;
  static {
    restoreDefaultClock();
  }
  
  protected Clock() {
    this(false);
  }
  
  protected Clock(boolean setAsInstance) {
    if (setAsInstance) instance = this;
  }

  abstract long currentClockTimeInMillis() ;

  Date currentClockDate() {
    return new Date(currentClockTimeInMillis());
  }
  
  public static long currentTimeInMillis() {
      return instance.currentClockTimeInMillis();
  }

  public static Date currentDate() {
    return instance.currentClockDate();
  }
  
  public static void restoreDefaultClock() {
    Clock.instance = SYSTEM_CLOCK;
}

}

class SystemClock extends Clock {
  @Override
  long currentClockTimeInMillis() {
    return System.currentTimeMillis();
  }  
}
