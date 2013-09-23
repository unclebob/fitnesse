package fit.decorator.performance;

import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.Timer;

public class TimeRange extends TimeBasedFixtureDecorator {
  public static final String MIN_TIME = "minTime";
  public static final String MAX_TIME = "maxTime";
  protected long maxTime;
  private long minTime;

  public TimeRange() {
    super();
  }

  TimeRange(Timer stopWatch) {
    super(stopWatch);
  }

  protected void run(Fixture fixture, Parse table) {
    super.run(fixture, table);
    summary.put(ACTUAL_TIME_TAKEN, new Long(elapsedTime));
  }

  protected void setupDecorator(String[] arguments) throws InvalidInputException {
    if (arguments.length != 2) {
      throw new InvalidInputException("Time range must be specified");
    }
    minTime = Long.parseLong(arguments[0]);
    summary.put(MIN_TIME, new Long(minTime));
    maxTime = Long.parseLong(arguments[1]);
    summary.put(MAX_TIME, new Long(maxTime));
  }

  protected void updateColumnsBasedOnResults(Parse table) {
    updateColumns(table.parts.parts.more, elapsedTime, minTime, false);
    updateColumns(table.parts.parts.more.more.more, elapsedTime, maxTime, true);
  }
}
