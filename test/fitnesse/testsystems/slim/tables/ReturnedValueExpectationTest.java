// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.testsystems.slim.results.SlimTestResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReturnedValueExpectationTest {
  private SlimTestContextImpl testContext;

  @Before
  public void setup() {
    testContext = new SlimTestContextImpl();
  }

  private void assertExpectationMessage(String expected, String value, String message) throws Exception {
    TableScanner ts = new HtmlTableScanner("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    SlimTable slimTable = new DecisionTable(t, "id", testContext);
    SlimTable.RowExpectation expectation = slimTable.new ReturnedValueExpectation(0, 0);
    SlimTestResult testResult = expectation.evaluationMessage(value, expected);
    assertEquals(message, testResult.toString(expected));
    //assertEquals(message, expectation.getEvaluationMessage());
    assertEquals(0, expectation.getRow());
    assertEquals(0, expectation.getCol());
  }

  @Test
  public void passingMessage() throws Exception {
    assertExpectationMessage("expected", "expected", "pass(expected)");
  }

  @Test
  public void failingMesage() throws Exception {
    assertExpectationMessage("expected", "actual", "fail(a=actual;e=expected)");
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
  public void matchedSymbolIsReplaced() throws Exception {
    testContext.setSymbol("S", "Value");
    assertExpectationMessage("$S", "Value", "pass($S->[Value])");
  }

  @Test
  public void mismatchedSymbolIsReplaced() throws Exception {
    testContext.setSymbol("S", "Value");
    assertExpectationMessage("$S", "WrongValue", "fail(a=WrongValue;e=$S->[Value])");
  }

  @Test
  public void matchedUnboundSymbolIsNotReplaced() throws Exception {
    assertExpectationMessage("$S", "$S", "pass($S)");
  }

  @Test
  public void mismatchedUnboundSymbolIsNotReplaced() throws Exception {
    assertExpectationMessage("$S", "$X", "fail(a=$X;e=$S)");
  }

  @Test
  public void lessThanComparisons() throws Exception {
    assertExpectationMessage(" < 5.2 ", "3", "pass(3< 5.2)");
    assertExpectationMessage("   <   5.2 ", "2", "pass(2< 5.2)");
    assertExpectationMessage(" < 5.2  ", "6", "fail(6< 5.2)");
    assertExpectationMessage(" <  5.2  ", "2.8", "pass(2.8< 5.2)");
  }

  @Test
  public void NotGEComparisons() throws Exception {
    assertExpectationMessage(" !>= 5.2", "3", "pass(3!>= 5.2)");
    assertExpectationMessage(" !>= 5.2", "2", "pass(2!>= 5.2)");
    assertExpectationMessage(" !>= 5.2", "6", "fail(6!>= 5.2)");
    assertExpectationMessage(" !>= 5.2", "2.8", "pass(2.8!>= 5.2)");
  }

  @Test
  public void greaterThanComparison() throws Exception {
    assertExpectationMessage(" > 5.9", "8", "pass(8> 5.9)");
    assertExpectationMessage(" > 5.9", "3.6", "fail(3.6> 5.9)");
  }

  @Test
  public void notLEComparison() throws Exception {
    assertExpectationMessage(" !<= 5.9", "8", "pass(8!<= 5.9)");
    assertExpectationMessage(" !<= 5.9", "3.6", "fail(3.6!<= 5.9)");
  }

  @Test
  public void approximatelyEquals() throws Exception {
    assertExpectationMessage("~= 3.0", "2.95", "pass(2.95~= 3.0)");
    assertExpectationMessage("~= 3.0", "2.8", "fail(2.8~= 3.0)");
  }

  @Test
  public void notApproximatelyEqual() throws Exception {
    assertExpectationMessage("!~= 3.0", "2.95", "fail(2.95!~= 3.0)");
    assertExpectationMessage("!~= 3.0", "2.8", "pass(2.8!~= 3.0)");
  }


  @Test
  public void notEqualComparison() throws Exception {
    assertExpectationMessage(" != 5.9", "8", "pass(8!= 5.9)");
    assertExpectationMessage(" != 5.9", "5.9", "fail(5.9!= 5.9)");
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
    assertExpectationMessage(" >=  5.9 ", "8", "pass(8>= 5.9)");
    assertExpectationMessage(" >=  5.9 ", "5.9", "pass(5.9>= 5.9)");
    assertExpectationMessage(" >=  5.9 ", "3.6", "fail(3.6>= 5.9)");
  }

  @Test
  public void notLessThanComparison() throws Exception {
    assertExpectationMessage(" !<  5.9 ", "8", "pass(8!< 5.9)");
    assertExpectationMessage(" !<  5.9 ", "5.9", "pass(5.9!< 5.9)");
    assertExpectationMessage(" !<  5.9 ", "3.6", "fail(3.6!< 5.9)");
  }

  @Test
  public void lessOrEqualComparison() throws Exception {
    assertExpectationMessage(" <= 5.9 ", "2", "pass(2<= 5.9)");
    assertExpectationMessage(" <= 5.9 ", "5.9", "pass(5.9<= 5.9)");
    assertExpectationMessage(" <= 5.9 ", "8.3", "fail(8.3<= 5.9)");
  }

  @Test
  public void notGreaterThanComparison() throws Exception {
    assertExpectationMessage(" !> 5.9 ", "2", "pass(2!> 5.9)");
    assertExpectationMessage(" !> 5.9 ", "5.9", "pass(5.9!> 5.9)");
    assertExpectationMessage(" !> 5.9 ", "8.3", "fail(8.3!> 5.9)");
  }

  @Test
  public void openIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 < _ < 5.9", "4.3", "pass(2.1 < 4.3 < 5.9)");
    assertExpectationMessage(" 2.1 < _ < 5.9", "2.1", "fail(2.1 < 2.1 < 5.9)");
    assertExpectationMessage(" 2.1 < _ < 5.9", "8.3", "fail(2.1 < 8.3 < 5.9)");
  }

  @Test
  public void closedLeftIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 <= _ < 5.9", "4.3", "pass(2.1 <= 4.3 < 5.9)");
    assertExpectationMessage(" 2.1 <= _ < 5.9", "2.1", "pass(2.1 <= 2.1 < 5.9)");
    assertExpectationMessage(" 2.1 <= _ < 5.9", "8.3", "fail(2.1 <= 8.3 < 5.9)");
  }

  @Test
  public void closedRightIntervalComparison() throws Exception {
    assertExpectationMessage(" 2.1 < _ <= 5.9", "4.3", "pass(2.1 < 4.3 <= 5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "2.1", "fail(2.1 < 2.1 <= 5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "5.9", "pass(2.1 < 5.9 <= 5.9)");
    assertExpectationMessage(" 2.1 < _ <= 5.9", "8.3", "fail(2.1 < 8.3 <= 5.9)");
  }

  @Test
  public void negativeNumberInSimpleComparison() throws Exception {
    assertExpectationMessage(" < -2 ", "-3", "pass(-3< -2)");
    assertExpectationMessage(" < -3 ", "-2", "fail(-2< -3)");
  }

  @Test
  public void negativeNumberInRangeComparison() throws Exception {
    assertExpectationMessage(" -4 < _ < -2", "-3", "pass(-4 < -3 < -2)");
    assertExpectationMessage(" -4 < _ < -2", "3", "fail(-4 < 3 < -2)");
  }

  @Test
  public void simpleRegularExpression() throws Exception {
    assertExpectationMessage("=~/Bob/", "Bob", "pass(/Bob/ found in: Bob)");
  }

  @Test
  public void regularExpressionMatchesSomethingInsideActualResult() throws Exception {
    assertExpectationMessage("=~/Bob/", "My name is Bob Martin", "pass(/Bob/ found in: My name is Bob Martin)");
  }

  @Test
  public void complexRegularExpressionMatches() throws Exception {
    assertExpectationMessage("=~/B.b/", "Oh Bob, how could you.", "pass(/B.b/ found in: Oh Bob, how could you.)");
  }

  @Test
  public void simpleRegexFails() throws Exception {
    assertExpectationMessage("=~/Bob/", "Pete", "fail(/Bob/ not found in: Pete)");
  }
}
