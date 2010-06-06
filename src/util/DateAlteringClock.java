package util;

import java.util.Calendar;
import java.util.Date;

/**
 * Use an instance of this class to rebase the Date
 * reported by Clock.currentTimeInMillis()/currentDate()
 * or measured by a new TimeMeasurement().
 * @see #restoreDefaultClock()
 */
public class DateAlteringClock extends Clock {
  private static final SystemClock SYSTEM_CLOCK = new SystemClock();
  private final Calendar rebaseToDate;
  
  public DateAlteringClock(Date rebaseToDate) {
    super(true);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(rebaseToDate.getTime());
    this.rebaseToDate = calendar;
  }
  
  @Override
  public long currentClockTimeInMillis() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(SYSTEM_CLOCK.currentClockTimeInMillis());
    calendar.set(Calendar.YEAR, rebaseToDate.get(Calendar.YEAR));
    calendar.set(Calendar.MONTH, rebaseToDate.get(Calendar.MONTH));
    calendar.set(Calendar.DAY_OF_MONTH, rebaseToDate.get(Calendar.DAY_OF_MONTH));
    return calendar.getTimeInMillis();
  }

  public void restoreDefaultClock() {
      Clock.instance = SYSTEM_CLOCK;
  }

}
