package fitnesse.responders.run.slimResponder;

import fitnesse.wikitext.Utils;
import fitnesse.wikitext.widgets.TableWidget;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ReturnedValueExpectationTest {
  private String unescape(String x) {
    return Utils.unescapeWiki(Utils.unescapeHTML(x));
  }

  private void assertExpectationMessage(String expected, String value, String message) throws Exception {
    TableScanner ts = new HtmlTableScanner("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    SlimTable slimTable = new DecisionTable(t, "id");
    SlimTable.Expectation expectation = slimTable.makeReturnedValueExpectation(expected, "", 0, 0);
    assertEquals(message, HtmlTable.colorize(unescape(expectation.createEvaluationMessage(value, expected))));
  }


  @Test
  public void passingMessage() throws Exception {
    assertExpectationMessage("expected", "expected", "pass(expected)");
  }

  @Test
  public void failingMesage() throws Exception {
    assertExpectationMessage("expected", "actual", "[actual] fail(expected [expected])");
  }

  @Test
  public void evaluationMessageForBlankInput() throws Exception {
    assertExpectationMessage("", "", "pass(BLANK)");
  }

  @Test
  public void evaluationMessageForBlankExpectation() throws Exception {
    assertExpectationMessage("", "ignore", "ignore(ignore)");
  }

  @Test
  public void lessThanComparisons() throws Exception {
    assertExpectationMessage(" < 5.2", "3", "pass(3<5.2)");
    assertExpectationMessage(" < 5.2", "2", "pass(2<5.2)");
    assertExpectationMessage(" < 5.2", "6", "fail(6<5.2)");
    assertExpectationMessage(" < 5.2", "2.8", "pass(2.8<5.2)");
  }

  @Test
  public void greaterThanComparison() throws Exception {
    assertExpectationMessage(" > 5.9", "8", "pass(8>5.9)");
    assertExpectationMessage(" > 5.9", "3.6", "fail(3.6>5.9)");
  }

  @Test
  public void approximatelyEquals() throws Exception {
    assertExpectationMessage("~= 3.0", "2.95", "pass(2.95~=3.0)");
  }


  @Test
  public void notEqualComparison() throws Exception {
    assertExpectationMessage(" != 5.9", "8", "pass(8!=5.9)");
    assertExpectationMessage(" != 5.9", "5.9", "fail(5.9!=5.9)");
  }

  @Test
  public void greaterOrEqualComparison() throws Exception {
    assertExpectationMessage(" >=  5.9 ", "8", "pass(8>=5.9)");
    assertExpectationMessage(" >=  5.9 ", "5.9", "pass(5.9>=5.9)");
    assertExpectationMessage(" >=  5.9 ", "3.6", "fail(3.6>=5.9)");
  }

  @Test
  public void lessOrEqualComparison() throws Exception {
    assertExpectationMessage(" <= 5.9 ", "2", "pass(2<=5.9)");
    assertExpectationMessage(" <= 5.9 ", "5.9", "pass(5.9<=5.9)");
    assertExpectationMessage(" <= 5.9 ", "8.3", "fail(8.3<=5.9)");
  }

  @Test
  public void openIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 < _ < 5.9", "4.3", "pass(2.1<4.3<5.9)");
    assertExpectationMessage(" 2.1 < _ < 5.9", "2.1", "fail(2.1<2.1<5.9)");
    assertExpectationMessage(" 2.1 < _ < 5.9", "8.3", "fail(2.1<8.3<5.9)");
  }

  @Test
  public void closedLeftIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 <= _ < 5.9", "4.3", "pass(2.1<=4.3<5.9)");
    assertExpectationMessage(" 2.1 <= _ < 5.9", "2.1", "pass(2.1<=2.1<5.9)");
    assertExpectationMessage(" 2.1 <= _ < 5.9", "8.3", "fail(2.1<=8.3<5.9)");
  }

  @Test
  public void closedRightIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 < _ <= 5.9", "4.3", "pass(2.1<4.3<=5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "2.1", "fail(2.1<2.1<=5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "5.9", "pass(2.1<5.9<=5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "8.3", "fail(2.1<8.3<=5.9)");
  }
}
