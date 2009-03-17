package fit.decorator.performance;

import java.text.ParseException;

import fit.ColumnFixture;
import fit.Counts;
import fit.Parse;
import fit.decorator.FixtureDecoratorTestCase;
import fit.decorator.Loop;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.TestCaseHelper;

public class MaxTimeTest extends FixtureDecoratorTestCase {
  private static final String FIRST_HTML_ROW = "<tr><td>" + MaxTime.class.getName()
    + "</td><td>100</td><td>milliseconds</td></tr>";
  private MaxTime decorator = new MaxTime();

  public void testRunShouldMeasureTimeTakenToExecuteDoTableMethodOnGivenFixture() throws Exception {
    String fitPage = "<table><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    MaxTime fixture = new MaxTime(stopWatch);
    fixture.run(new ColumnFixture(), new Parse(fitPage));
    assertEquals(ELAPSED, ((Long) fixture.summary.get(MaxTime.ACTUAL_TIME_TAKEN)).longValue());
  }

  public void testSetupDecoratorMustThrowInvalidInputExceptionIfMaxTimeIsNotSpecified() throws ParseException {
    try {
      decorator.setupDecorator(new String[0]);
      fail("Should blow up ");
    } catch (InvalidInputException e) {
      // expected
    }
  }

  public void testSetupDecoratorShouldAddMaxTimeToSummary() throws Exception {
    decorator.setupDecorator(new String[]
      {"80"});
    assertEquals(80, ((Long) decorator.summary.get(MaxTime.MAX_TIME)).longValue());
  }

  public void testShouldFailIfActualExecutionTimeIsGreaterThanMaxtime() throws Exception {
    String fitPage = "<table><tr><td>" + MaxTime.class.getName() + "</td><td>19</td><td>milliseconds</td>"
      + "</tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(3, 1, 0, 0);
    executeAndAssert(expected, fitPage, new MaxTime(stopWatch));
  }

  public void testShouldPassIfActualExecutionTimeIsEqualToMaxtime() throws Exception {
    String fitPage = "<table><tr><td>" + MaxTime.class.getName()
      + "</td><td>20</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(4, 0, 0, 0);
    executeAndAssert(expected, fitPage, new MaxTime(stopWatch));
  }

  public void testShouldPassIfActualExecutionTimeIsLessThanMaxtime() throws Exception {
    String fitPage = "<table><tr><td>" + MaxTime.class.getName()
      + "</td><td>80</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(4, 0, 0, 0);
    executeAndAssert(expected, fitPage, new MaxTime(stopWatch));
  }

  public void testShouldWorkIfFitureDecoratorsArePiped() throws Exception {
    String fitPage = "<table><tr><td>" + MaxTime.class.getName()
      + "</td><td>80</td><td>milliseconds</td></tr><tr><td>" + Loop.class.getName()
      + "</td><td>3</td><td>time</td></tr><tr><td>" + MaxTime.class.getName()
      + "</td><td>80</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(13, 0, 0, 0);
    executeAndAssert(expected, fitPage, new MaxTime(stopWatch));
  }

  protected String geDecoratorHTMLRow() {
    return FIRST_HTML_ROW;
  }

  protected int numberOfAssertionsOnDecorator() {
    return 1;
  }
}
