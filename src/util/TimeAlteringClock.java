package util;

import java.util.Date;

/**
 * Use an instance of this class to alter the time
 * reported by Clock.currentTimeInMillis()/currentDate()
 * or measured by a new TimeMeasurement().
 * @see #restoreDefaultClock()
 */
public class TimeAlteringClock extends Clock {

  private final Clock priorInstance;
  private final long[] clockTimesInMillis;
  private int nextTimePos = 0;

  public TimeAlteringClock(long... clockTimesInMillis) {
    this(Clock.instance, clockTimesInMillis);
  }

  public TimeAlteringClock(Date... clockDates) {
    this(datesToMillis(clockDates));
  }
  
  private static long[] datesToMillis(Date[] clockDates) {
    long[] times = new long[clockDates.length];
    int i = 0;
    for (Date date : clockDates) {
      times[i++] = date.getTime();
    }
    return times;
  }

  private TimeAlteringClock(Clock priorInstance, long[] clockTimesInMillis) {
    super(true);
    this.priorInstance = priorInstance;
    this.clockTimesInMillis = clockTimesInMillis;
  }
  
  public void restoreDefaultClock() {
    Clock.instance = priorInstance;
  }

  @Override
  long currentClockTimeInMillis() {
    try {
      return clockTimesInMillis[nextTimePos++];
    } catch (ArrayIndexOutOfBoundsException e) {
      restoreDefaultClock();
      return Clock.currentTimeInMillis();
    }
  }

}
