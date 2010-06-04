// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.util.Date;

public abstract class Clock {
  private static Clock instance = new SystemClock();
  
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
}

class SystemClock extends Clock {
  @Override
  long currentClockTimeInMillis() {
    return System.currentTimeMillis();
  }  
}

class GlobalClock extends Clock {
  @Override
  long currentClockTimeInMillis() {
    return currentTimeInMillis();
  }  
}