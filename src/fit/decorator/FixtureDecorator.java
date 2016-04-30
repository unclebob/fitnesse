package fit.decorator;

import java.util.ArrayList;
import java.util.List;

import fit.Fixture;
import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.Table;

public abstract class FixtureDecorator extends Fixture {
  static final String ENCAPSULATED_FIXTURE_NAME = "EncapsulatedFixtureName";

  @Override
  public void doTable(Parse table) {
    if (table.parts.more == null) {
      return;
    }
    validateDecoratorInput(table);
    Parse actualHeader = table.parts.more.parts;
    String encapsulatedFixtureName = actualHeader.text();
    super.summary.put(ENCAPSULATED_FIXTURE_NAME, encapsulatedFixtureName);
    Fixture fixture = loadFixture(actualHeader, encapsulatedFixtureName);
    if (fixture != null) {
      execute(fixture, table);
      super.summary.putAll(fixture.summary);
      counts.tally(fixture.counts);
    }
  }

  protected abstract void setupDecorator(String[] args) throws InvalidInputException;

  protected abstract void updateColumnsBasedOnResults(Parse table);

  protected void run(Fixture fixture, Parse table) {
    fixture.doTable(table);
  }

  private void execute(Fixture fixture, Parse table) {
    Table t = new Table(table);
    Parse firstRow = t.stripFirstRow();
    run(fixture, table);
    t.insertAsFirstRow(firstRow);
    updateColumnsBasedOnResults(table);
  }

  private Fixture loadFixture(Parse actualHeader, String encapsulatedFixtureName) {
    Fixture fixture = null;
    try {
      fixture = loadFixture(encapsulatedFixtureName);
    } catch (Throwable e) { // NOSONAR
      exception(actualHeader, e);
    }
    return fixture;
  }

  private void validateDecoratorInput(Parse table) {
    setAlternativeArgs(table);
    try {
      setupDecorator(super.args);
    } catch (InvalidInputException e) {
      exception(table.parts, e);
    }
  }

  void setAlternativeArgs(Parse table) {
    List<String> argumentList = new ArrayList<>();
    Parse columns = table.parts.parts;
    int size = columns.size();
    for (int i = 0; i < size / 2; ++i) {
      String columnValue = columns.at(i * 2 + 1).text();
      columnValue = escapeExpectedAndActualString(columnValue);
      argumentList.add(columnValue);
    }
    args = argumentList.toArray(new String[argumentList.size()]);
  }

  private String escapeExpectedAndActualString(String columnValue) {
    int index = columnValue.indexOf("actual");
    if (index == -1) {
      index = columnValue.length();
    }
    return columnValue.substring(0, index);
  }
}
