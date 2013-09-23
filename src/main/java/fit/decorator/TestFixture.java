package fit.decorator;

import fit.Fixture;
import fit.Parse;
import fit.decorator.util.Table;

public class TestFixture extends Fixture {
  public static final String TABLE_CONTENTS = "tableContents";

  public TestFixture() {
    super.summary.put(FixtureDecorator.ENCAPSULATED_FIXTURE_NAME, TestFixture.class.getName());
  }

  public void doTable(Parse table) {
    super.summary.put(TABLE_CONTENTS, new Table(table).toString());
  }
}
