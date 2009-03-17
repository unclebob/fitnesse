package fit.decorator;

import java.text.ParseException;

import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.TestCaseHelper;

public class LoopTest extends FixtureDecoratorTestCase {
  private static final String FIRST_HTML_ROW = "<tr><td>" + Loop.class.getName()
    + "</td><td>1</td><td>times</td></tr>";
  private FixtureDecorator decorator = new Loop();

  public void testSetupDecoratorShouldThrowInvalidInputExceptionIfLoopCountIsNotSpecified() throws ParseException {
    try {
      decorator.setupDecorator(new String[0]);
      fail("Should blow up");
    } catch (InvalidInputException e) {
      assertEquals("Loop count must be specified", e.getMessage());
    }
  }

  public void testSetupDecoratorShouldAddLoopCountToSummary() throws Exception {
    decorator.setupDecorator(new String[]
      {"5"});
    assertEquals(5, ((Long) decorator.summary.get(Loop.COUNT)).longValue());
  }

  public void testShouldExecuteDoTableMethodLoopCounterNumberOfTimes() throws Exception {
    String fitPage = "<table><tr><td>" + Loop.class.getName()
      + "</td><td>5</td><td>times</td></tr><tr><td>eg.Division</td></tr>"
      + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
      + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>12.6</td><td>3</td><td>4.2</td></tr>"
      + "<tr><td>100</td><td>4</td><td>25</td></tr></table>";
    decorator.doTable(new Parse(fitPage));
    TestCaseHelper.assertCounts(TestCaseHelper.counts(15, 0, 0, 0), decorator.counts);
  }

  protected String geDecoratorHTMLRow() {
    return FIRST_HTML_ROW;
  }

  protected int numberOfAssertionsOnDecorator() {
    return 0;
  }
}
