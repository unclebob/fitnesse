// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.wikitext.Utils;

public class ReturnedValueExpectationTest {
  private MockSlimTestContext testContext;

  private void assertExpectationMessage(String expected, String value, String message) throws Exception {
    TableScanner ts = new HtmlTableScanner("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    SlimTable slimTable = new DecisionTable(t, "id", testContext);
    SlimTable.Expectation expectation = slimTable.makeReturnedValueExpectation(expected, "instructionId", 1, 2);
    assertEquals(message, HtmlTable.colorize(expectation.evaluationMessage(value, expected)));
    assertEquals(message, expectation.getEvaluationMessage());
    assertEquals(2, expectation.getRow());
    assertEquals(1, expectation.getCol());
    assertEquals("instructionId", expectation.getInstructionTag());
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
  public void NotGEComparisons() throws Exception {
    assertExpectationMessage(" !>= 5.2", "3", "pass(3!>=5.2)");
    assertExpectationMessage(" !>= 5.2", "2", "pass(2!>=5.2)");
    assertExpectationMessage(" !>= 5.2", "6", "fail(6!>=5.2)");
    assertExpectationMessage(" !>= 5.2", "2.8", "pass(2.8!>=5.2)");
  }

  @Test
  public void greaterThanComparison() throws Exception {
    assertExpectationMessage(" > 5.9", "8", "pass(8>5.9)");
    assertExpectationMessage(" > 5.9", "3.6", "fail(3.6>5.9)");
  }

  @Test
  public void notLEComparison() throws Exception {
    assertExpectationMessage(" !<= 5.9", "8", "pass(8!<=5.9)");
    assertExpectationMessage(" !<= 5.9", "3.6", "fail(3.6!<=5.9)");
  }

  @Test
  public void approximatelyEquals() throws Exception {
    assertExpectationMessage("~= 3.0", "2.95", "pass(2.95~=3.0)");
    assertExpectationMessage("~= 3.0", "2.8", "fail(2.8~=3.0)");
  }

  @Test
  public void notApproximatelyEqual() throws Exception {
    assertExpectationMessage("!~= 3.0", "2.95", "fail(2.95!~=3.0)");
    assertExpectationMessage("!~= 3.0", "2.8", "pass(2.8!~=3.0)");
  }


  @Test
  public void notEqualComparison() throws Exception {
    assertExpectationMessage(" != 5.9", "8", "pass(8!=5.9)");
    assertExpectationMessage(" != 5.9", "5.9", "fail(5.9!=5.9)");
  }

  @Test
  public void equalComparison() throws Exception {
    assertExpectationMessage("=3", "03", "pass(03=3)");
    assertExpectationMessage("=3", " 3 ", "pass( 3 =3)");
    assertExpectationMessage("=3", ".2", "fail(.2=3)");
    assertExpectationMessage("=3.1", "3.1", "pass(3.1=3.1)");
    assertExpectationMessage("=3.1", "3.10001", "fail(3.10001=3.1)");
  }

  @Test
  public void greaterOrEqualComparison() throws Exception {
    assertExpectationMessage(" >=  5.9 ", "8", "pass(8>=5.9)");
    assertExpectationMessage(" >=  5.9 ", "5.9", "pass(5.9>=5.9)");
    assertExpectationMessage(" >=  5.9 ", "3.6", "fail(3.6>=5.9)");
  }

  @Test
  public void notLessThanComparison() throws Exception {
    assertExpectationMessage(" !<  5.9 ", "8", "pass(8!<5.9)");
    assertExpectationMessage(" !<  5.9 ", "5.9", "pass(5.9!<5.9)");
    assertExpectationMessage(" !<  5.9 ", "3.6", "fail(3.6!<5.9)");
  }

  @Test
  public void lessOrEqualComparison() throws Exception {
    assertExpectationMessage(" <= 5.9 ", "2", "pass(2<=5.9)");
    assertExpectationMessage(" <= 5.9 ", "5.9", "pass(5.9<=5.9)");
    assertExpectationMessage(" <= 5.9 ", "8.3", "fail(8.3<=5.9)");
  }

  @Test
  public void notGreaterThanComparison() throws Exception {
    assertExpectationMessage(" !> 5.9 ", "2", "pass(2!>5.9)");
    assertExpectationMessage(" !> 5.9 ", "5.9", "pass(5.9!>5.9)");
    assertExpectationMessage(" !> 5.9 ", "8.3", "fail(8.3!>5.9)");
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
