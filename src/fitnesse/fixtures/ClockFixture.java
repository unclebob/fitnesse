package fitnesse.fixtures;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import util.Clock;
import util.DateAlteringClock;
import util.DateTimeUtil;

public class ClockFixture {
  public void freezeClockAt(String dateTime) throws ParseException {
    new DateAlteringClock(DateTimeUtil.getDateFromString(dateTime)).freeze();
  }
  
}
