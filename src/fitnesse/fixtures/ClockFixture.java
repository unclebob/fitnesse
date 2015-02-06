package fitnesse.fixtures;

import java.text.ParseException;

import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;

public class ClockFixture {
  public void freezeClockAt(String dateTime) throws ParseException {
    new DateAlteringClock(DateTimeUtil.getDateFromString(dateTime)).freeze();
  }
  
}
