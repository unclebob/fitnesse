package fit.decorator.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;

import fit.Counts;
import fit.decorator.FixtureDecoratorTestCase;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.TestCaseHelper;
import org.junit.Test;

public class TimeRangeTest extends FixtureDecoratorTestCase {
  // this is used only for the FixtureDecoratorTestCase tests which don't check the time range
  // so make it really big to prevent intermittent failures
  private static final String FIRST_HTML_ROW = "<tr><td>" + TimeRange.class.getName()
    + "</td><td>0</td><td>milliseconds min and max</td><td>999999999</td><td>milliseconds</td></tr>";
  private TimeRange decorator = new TimeRange();

  protected String geDecoratorHTMLRow() {
    return FIRST_HTML_ROW;
  }

  protected int numberOfAssertionsOnDecorator() {
    return 2;
  }

  @Test
  public void testSetupDecoratorMustThrowInvalidInputExceptionIfTimeRangeIsNotSpecified() throws ParseException {
    try {
      decorator.setupDecorator(new String[]
        {"10"});
      fail("Should blow up ");
    } catch (InvalidInputException e) {
      // expected
    }
  }

  @Test
  public void testSetupDecoratorShouldAddTimeRangeToSummary() throws Exception {
    decorator.setupDecorator(new String[]
      {"10", "80"});
    assertEquals(10, ((Long) decorator.summary.get(TimeRange.MIN_TIME)).longValue());
    assertEquals(80, ((Long) decorator.summary.get(MaxTime.MAX_TIME)).longValue());
  }

  @Test
  public void testShouldFailIfActualExecutionTimeGreaterThanMaxTime() throws Exception {
    String fitPage = "<table><tr><td>fit.decorator.TimeRange</td><td>15</td><td>milliseconds min and max</td>"
      + "<td>19</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(4, 1, 0, 0);
    executeAndAssert(expected, fitPage, new TimeRange(stopWatch));
  }

  @Test
  public void testShouldFailIfActualExecutionTimeLessThanMinTime() throws Exception {
    String fitPage = "<table><tr><td>fit.decorator.TimeRange</td><td>21</td><td>milliseconds min and max</td>"
      + "<td>25</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(4, 1, 0, 0);
    executeAndAssert(expected, fitPage, new TimeRange(stopWatch));
  }

  @Test
  public void testShouldPassIfActualExecutionTimeIsWithinTheRange() throws Exception {
    String fitPage = "<table><tr><td>fit.decorator.TimeRange</td><td>15</td><td>milliseconds min and max</td>"
      + "<td>25</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(5, 0, 0, 0);
    executeAndAssert(expected, fitPage, new TimeRange(stopWatch));
  }

  @Test
  public void testShouldPassIfActualExecutionTimeIsEqualToMinTime() throws Exception {
    String fitPage = "<table><tr><td>fit.decorator.TimeRange</td><td>20</td><td>milliseconds min and max</td>"
      + "<td>25</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(5, 0, 0, 0);
    executeAndAssert(expected, fitPage, new TimeRange(stopWatch));
  }

  @Test
  public void testShouldPassIfActualExecutionTimeIsEqualToMaxTime() throws Exception {
    String fitPage = "<table><tr><td>fit.decorator.TimeRange</td><td>15</td><td>milliseconds min and max</td>"
      + "<td>20</td><td>milliseconds</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    Counts expected = TestCaseHelper.counts(5, 0, 0, 0);
    executeAndAssert(expected, fitPage, new TimeRange(stopWatch));
  }
}
