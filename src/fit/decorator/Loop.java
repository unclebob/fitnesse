package fit.decorator;

import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;

public class Loop extends FixtureDecorator {
  public static final String COUNT = "count";
  private int counter;

  @Override
  protected void run(Fixture fixture, Parse table) {
    for (int i = 0; i < counter; i++) {
      super.run(fixture, table);
    }
  }

  @Override
  protected void setupDecorator(String[] arguments) throws InvalidInputException {
    if (arguments.length != 1) {
      throw new InvalidInputException("Loop count must be specified");
    }
    counter = Integer.parseInt(arguments[0]);
    summary.put(COUNT, new Long(counter));
  }

  @Override
  protected void updateColumnsBasedOnResults(Parse table) {
    // Nothing to do
  }
}
