// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FriendlyErrorTest {
  //Test the FitFailureException mechanism.  If this works, then all of the FitFailureException derivatives ought
  //to be working too.
  @Test
  public void testCantFindFixture() throws Exception {
    String pageString = "<table><tr><td>NoSuchFixture</td></tr></table>";
    Parse page = new Parse(pageString);
    Fixture fixture = new Fixture();
    fixture.doTables(page);
    String fixtureName = page.at(0, 0, 0).body;
    assertTrue(fixtureName.contains("Could not find fixture: NoSuchFixture."));
  }

  @Test
  public void testNoSuchMethod() throws Exception {
    final String[][] table = {
      {"fitnesse.fixtures.ColumnFixtureTestFixture"},
      {"no such method?"}
    };
    Parse page = FixtureTest.executeFixture(table);
    String columnHeader = page.at(0, 1, 0).body;
    assertTrue(columnHeader.contains("Could not find method: no such method?."));
  }

  @Test
  public void testParseFailure() throws Exception {
    final String[][] table = {
      {"fitnesse.fixtures.ColumnFixtureTestFixture"},
      {"input", "output?"},
      {"1", "alpha"}
    };
    Parse page = FixtureTest.executeFixture(table);
    String colTwoResult = page.at(0, 2, 1).body;
    assertTrue(colTwoResult.contains("Could not parse: alpha, expected type: int"));
  }

  @Test
  public void testExceptionInMethod() throws Exception {
    final String[][] table = {
      {"fitnesse.fixtures.ColumnFixtureTestFixture"},
      {"input", "exception?"},
      {"1", "true"}
    };
    Parse page = FixtureTest.executeFixture(table);
    String colTwoResult = page.at(0, 2, 1).body;
    assertTrue(colTwoResult.contains("I thowed up"));
  }
}
