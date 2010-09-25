package util;

import java.util.Calendar;
import java.util.Date;

/**
 * Use an instance of this class to rebase the Date
 * reported by Clock.currentTimeInMillis()/currentDate()
 * or measured by a new TimeMeasurement().
 * @see Clock#restoreDefaultClock()
 */
public class DateAlteringClock extends Clock {
  private long rebaseToTime;
  private final long baseSystemTime;
  private boolean frozen, advanceOnEachQuery;
  
  public DateAlteringClock(Date rebaseToDate) {
    super(true);
    this.rebaseToTime = rebaseToDate.getTime();
    this.baseSystemTime = SYSTEM_CLOCK.currentClockTimeInMillis();
  }
  
  @Override
  public long currentClockTimeInMillis() {
    if (frozen) {
      return rebaseToTime;
    } else if (advanceOnEachQuery) {
      return ++rebaseToTime;
    }
    return rebaseToTime + SYSTEM_CLOCK.currentClockTimeInMillis() - baseSystemTime;
  }

  public DateAlteringClock freeze() {
    frozen = true;
    return this;
  }

  public DateAlteringClock advanceMillisOnEachQuery() {
    advanceOnEachQuery = true;
    return this;
  }

}
