package fit.decorator;

import java.text.ParseException;

import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.TestCaseHelper;

public class CopyAndAppendLastRowTest extends FixtureDecoratorTestCase {
  private static final String FIRST_HTML_ROW = "<tr><td>" + CopyAndAppendLastRow.class.getName()
    + "</td><td>0</td><td>times</td></tr>";
  private static final int COUNTER = 5;
  private FixtureDecorator decorator = new CopyAndAppendLastRow();

  protected String geDecoratorHTMLRow() {
    return FIRST_HTML_ROW;
  }

  protected int numberOfAssertionsOnDecorator() {
    return 0;
  }

  public void testSetupDecoratorShouldThrowInvalidInputExceptionIfCounterIsNotSpecified() throws ParseException {
    try {
      decorator.setupDecorator(new String[0]);
      fail("Should blow up");
    } catch (InvalidInputException e) {
      assertEquals("Count for number of times to add the last row must be specified", e.getMessage());
    }
  }

  public void testSetupDecoratorShouldAddCounterToSummary() throws Exception {
    decorator.setupDecorator(new String[]
      {String.valueOf(COUNTER)});
    assertEquals(COUNTER, ((Integer) decorator.summary.get(CopyAndAppendLastRow.NUMBER_OF_TIMES)).intValue());
  }

  public void testShouldLeaveTableAsItIsIfCounterValueIsZero() throws Exception {
    String fitPage = "<table>" + FIRST_HTML_ROW + "<tr><td>fit.decorator.TestFixture</td></tr></table>";
    decorator.doTables(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(0, 0, 0, 0), decorator.counts);
  }

  public void testShouldAddOneRowIfCounterValueIsOne() throws Exception {
    String fitPage = "<table><tr><td>" + CopyAndAppendLastRow.class.getName() + "</td><td>1"
      + "</td><td>times</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    decorator.doTables(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(2, 0, 0, 0), decorator.counts);
  }

  public void testShouldLeaveTableAsItIsIfTotalRowsAreLessThanThree() throws Exception {
    String fitPage = "<table><tr><td>" + CopyAndAppendLastRow.class.getName() + "</td>"
      + "<td>0</td><td>times</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr></table>";
    decorator.doTables(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(0, 0, 0, 0), decorator.counts);
  }

  public void testShouldAppendLastRowCounterNumberOfTimes() throws Exception {
    String fitPage = "<table><tr><td>" + CopyAndAppendLastRow.class.getName() + "</td><td>" + COUNTER
      + "</td><td>times</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
    decorator.doTable(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(COUNTER + 1, 0, 0, 0), decorator.counts);
  }

  public void testShouldAppendOnlyTheLastRowCounterNumberOfTimes() throws Exception {
    String fitPage = "<table><tr><td>" + CopyAndAppendLastRow.class.getName() + "</td><td>" + COUNTER
      + "</td><td>times</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    decorator.doTable(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(COUNTER + 3, 0, 0, 0), decorator.counts);
  }

}
