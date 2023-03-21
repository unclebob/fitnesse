package fitnesse.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Use an instance of this class to rebase the Date
 * reported by Clock.currentTimeInMillis()/currentDate()
 * or measured by a new TimeMeasurement().
 * @see Clock#restoreDefaultClock()
 */
public class DateAlteringClock extends Clock {
  private long rebaseToTime;

  private TimeZone timeZone;
  private final long baseSystemTime;
  private boolean frozen, advanceOnEachQuery;

  public DateAlteringClock(Date rebaseToDate) {
    this(rebaseToDate, TimeZone.getTimeZone("Etc/GMT-14"));
  }

  public DateAlteringClock(Date rebaseToDate, TimeZone timeZone) {
    super(true);
    this.rebaseToTime = rebaseToDate.getTime();
    this.timeZone = timeZone;
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

  @Override
  protected TimeZone getTimeZone() {
    return timeZone;
  }

  public DateAlteringClock freeze() {
    frozen = true;
    return this;
  }

  public DateAlteringClock elapse(long ms) {
    rebaseToTime += ms;
    return this;
  }

  public DateAlteringClock advanceMillisOnEachQuery() {
    advanceOnEachQuery = true;
    return this;
  }

}
