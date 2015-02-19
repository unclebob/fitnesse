package fitnesse.fixtures;

import java.text.ParseException;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;

public class ClockFixture {
  public void freezeClockAt(String dateTime) throws ParseException {
    System.out.println("Freezing time at " + dateTime);
    new DateAlteringClock(DateTimeUtil.getDateFromString(dateTime)).freeze();
  }

  public String simulationDate() {
    return DateTimeUtil.formatDate(Clock.currentDate());
  }
}
