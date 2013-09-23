package fit.decorator;

import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.Table;

public class CopyAndAppendLastRow extends FixtureDecorator {
  public static final String NUMBER_OF_TIMES = "numberOfTimes";
  private int numberOfTimes;

  protected void run(Fixture fixture, Parse table) {
    Table t = new Table(table);
    t.copyAndAppendLastRow(numberOfTimes);
    super.run(fixture, t.table());
  }

  protected void setupDecorator(String[] arguments) throws InvalidInputException {
    if (arguments.length != 1) {
      throw new InvalidInputException("Count for number of times to add the last row must be specified");
    }
    numberOfTimes = Integer.parseInt(arguments[0]);
    summary.put(NUMBER_OF_TIMES, new Integer(numberOfTimes));
  }

  protected void updateColumnsBasedOnResults(Parse table) {
    // Nothing to do
  }
}
