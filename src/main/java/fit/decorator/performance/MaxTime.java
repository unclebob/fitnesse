package fit.decorator.performance;

import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.Timer;

public class MaxTime extends TimeBasedFixtureDecorator {
  public static final String MAX_TIME = "maxTime";
  protected long maxTime;

  public MaxTime() {
    super();
  }

  MaxTime(Timer stopWatch) {
    super(stopWatch);
  }

  protected void run(Fixture fixture, Parse table) {
    super.run(fixture, table);
    summary.put(ACTUAL_TIME_TAKEN, new Long(elapsedTime));
  }

  protected void setupDecorator(String[] arguments) throws InvalidInputException {
    if (arguments.length != 1) {
      throw new InvalidInputException("Max Time must be specified");
    }
    maxTime = Long.parseLong(arguments[0]);
    summary.put(MAX_TIME, new Long(maxTime));
  }

  protected void updateColumnsBasedOnResults(Parse table) {
    updateColumns(table.parts.parts.more, elapsedTime, maxTime, true);
  }
}
