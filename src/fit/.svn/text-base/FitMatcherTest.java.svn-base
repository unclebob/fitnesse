// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.TestCase;

public class FitMatcherTest extends TestCase {
  private void assertMatch(String expression, Number parameter) throws Exception {
    FitMatcher matcher = new FitMatcher(expression, parameter);
    assertTrue(matcher.matches());
  }

  private void assertNoMatch(String expression, Number parameter) throws Exception {
    FitMatcher matcher = new FitMatcher(expression, parameter);
    assertFalse(matcher.matches());
  }

  private void assertException(String expression, Object parameter) {
    FitMatcher matcher = new FitMatcher(expression, parameter);
    try {
      matcher.matches();
      fail();
    }
    catch (Exception e) {
    }
  }

  public void testSimpleMatches() throws Exception {
    assertMatch("_<3", new Integer(2));
    assertNoMatch("_<3", new Integer(3));
    assertMatch("_<4", new Integer(3));
    assertMatch("_ < 9", new Integer(4));
    assertMatch("<3", new Integer(2));
    assertMatch(">4", new Integer(5));
    assertMatch(">-3", new Integer(-2));
    assertMatch("<3.2", new Double(3.1));
    assertNoMatch("<3.2", new Double(3.3));
    assertMatch("<=3", new Double(3));
    assertMatch("<=3", new Double(2));
    assertNoMatch("<=3", new Double(4));
    assertMatch(">=2", new Double(2));
    assertMatch(">=2", new Double(3));
    assertNoMatch(">=2", new Double(1));
  }

  public void testExceptions() throws Exception {
    assertException("X", new Integer(1));
    assertException("<32", "xxx");
  }

  public void testMessage() throws Exception {
    FitMatcher matcher = new FitMatcher("_>25", new Integer(3));
    assertEquals("<b>3</b>>25", matcher.message());
    matcher = new FitMatcher(" < 32", new Integer(5));
    assertEquals("<b>5</b> < 32", matcher.message());
  }

  public void testTrichotomy() throws Exception {
    assertMatch("5<_<32", new Integer(8));
    assertNoMatch("5<_<32", new Integer(5));
    assertNoMatch("5<_<32", new Integer(32));
    assertMatch("10>_>5", new Integer(6));
    assertNoMatch("10>_>5", new Integer(10));
    assertNoMatch("10>_>5", new Integer(5));
    assertMatch("10>=_>=5", new Integer(10));
    assertMatch("10>=_>=5", new Integer(5));
  }

}
