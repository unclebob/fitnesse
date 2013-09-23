package fit.decorator.performance;

import fit.Fixture;
import fit.Parse;
import fit.decorator.FixtureDecorator;
import fit.decorator.util.DefaultTimer;
import fit.decorator.util.Timer;

public abstract class TimeBasedFixtureDecorator extends FixtureDecorator {

  public static final String ACTUAL_TIME_TAKEN = "actualTimeTaken";
  protected Timer stopWatch;
  protected long elapsedTime;

  public TimeBasedFixtureDecorator(Timer watch) {
    super();
    this.stopWatch = watch;
  }

  TimeBasedFixtureDecorator() {
    super();
    this.stopWatch = new DefaultTimer();
  }

  protected void run(Fixture fixture, Parse table) {
    stopWatch.start();
    super.run(fixture, table);
    elapsedTime = stopWatch.elapsed();
  }

  protected void updateColumns(Parse column, double actual, double expected, boolean greaterThan) {
    if (checkFailureBasedOnGreaterThanOrLessThan(actual, expected, greaterThan)) {
      super.wrong(column, Double.toString(actual));
    } else {
      super.right(column);
      column.addToBody("<hr>actual " + Double.toString(actual));
    }
  }

  private boolean checkFailureBasedOnGreaterThanOrLessThan(double actual, double expected, boolean greaterThan) {
    return greaterThan ? actual > expected : actual < expected;
  }

}
