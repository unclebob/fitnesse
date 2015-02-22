// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FixtureTest {
  private Locale saveLocale;

  static class HasParseMethod {
    public static Object parse(String s) {
      return s + " found";
    }
  }

  static class HasNoParseMethod {
  }

  @Before
  public void setUp() throws Exception {
    saveLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
  }

  @After
  public void tearDown() throws Exception {
    Locale.setDefault(saveLocale);
  }

  @Test
  public void testHasParseMethod() throws Exception {
    assertTrue(Fixture.hasParseMethod(HasParseMethod.class));
    assertFalse(Fixture.hasParseMethod(HasNoParseMethod.class));
  }

  @Test
  public void testCallParseMethod() throws Exception {
    Object o = Fixture.callParseMethod(HasParseMethod.class, "target");
    assertTrue(o instanceof String);
    String s = (String) o;
    assertEquals("target found", s);
  }

  @Test
  public void testObjectWithParseMethod() throws Exception {
    Fixture f = new Fixture();
    Object o = f.parse("target", HasParseMethod.class);
    assertTrue(o instanceof String);
    assertEquals("target found", (String) o);

    try {
      f.parse("target", HasNoParseMethod.class);
    }
    catch (Exception e) {
      assertTrue(e.getMessage().startsWith("Could not parse"));
    }
  }

  @Test
  public void testScientificDouble() throws Exception {
    Fixture f = new Fixture();
    Object o = f.parse("13.4", ScientificDouble.class);
    assertTrue(o instanceof ScientificDouble);
    assertEquals(new ScientificDouble(13.4), o);
  }

  @Test
  public void testRelationalMatching() throws Exception {
    final String[][] table = {
      {"fitnesse.fixtures.ColumnFixtureTestFixture"},
      {"input", "output?"},
      {"1", "_>0"}
    };
    Parse page = executeFixture(table);
    String colTwoResult = page.at(0, 2, 1).body;
    assertTrue(colTwoResult.contains("<b>1</b>>0"));
    String colTwoTag = page.at(0, 2, 1).tag;
    assertTrue(colTwoTag.contains("pass"));
  }

  @Test
  public void testNullAndBlankStrings() throws Exception {
    Fixture fixture = new Fixture();
    assertNull(fixture.parse("null", String.class));
    assertEquals("", fixture.parse("blank", String.class));

    TypeAdapter adapter = new TypeAdapter();
    assertEquals("null", adapter.toString((String) null));
    assertEquals("blank", adapter.toString(""));
  }

  @Test
  public void testEscape() {
    String junk = "!@#$%^*()_-+={}|[]\\:\";',./?`";
    assertEquals(junk, Fixture.escape(junk));
    assertEquals("", Fixture.escape(""));
    assertEquals("&lt;", Fixture.escape("<"));
    assertEquals("&lt;&lt;", Fixture.escape("<<"));
    assertEquals("x&lt;", Fixture.escape("x<"));
    assertEquals("&amp;", Fixture.escape("&"));
    assertEquals("&lt;&amp;&lt;", Fixture.escape("<&<"));
    assertEquals("&amp;&lt;&amp;", Fixture.escape("&<&"));
    assertEquals("a &lt; b &amp;&amp; c &lt; d", Fixture.escape("a < b && c < d"));
  }

  @Test
  public void testFixtureArguments() throws Exception {
    String[] args = getArgsForTableWith("<td>1</td>");
    assertEquals(1, args.length);
    assertEquals("1", args[0]);

    args = getArgsForTableWith("");
    assertEquals(0, args.length);

    args = getArgsForTableWith("<td>1</td><td>2</td>");
    assertEquals(2, args.length);
    assertEquals("1", args[0]);
    assertEquals("2", args[1]);
  }

  private String[] getArgsForTableWith(String argsDefinition) throws Exception {
    String prefix = "<table><tr><td>fit.Fixture</td>";
    String suffix = "</tr></table>";
    Parse table = new Parse(prefix + argsDefinition + suffix);
    Fixture f = new Fixture();
    f.getArgsForTable(table);
    return f.getArgs();
  }

  @Test
  public void testFixtureArgumentsWithEscapedSymbols() throws Exception {
    // "<", ">" are usually escaped (unless surrounded by !- .. -!)
    String[] args = getArgsForTableWith("<td>a &lt; 3 and b &gt; 1</td>");
    assertArrayEquals(new String[]{"a < 3 and b > 1"}, args);
  }

  @Test
  public void testFixtureArgumentsWithSpecialSymbols() throws Exception {
    // "<", ">" are usually escaped but may come in plan text from !- .. -!
    String[] args = getArgsForTableWith("<td>a < 3 and b > 1</td>");
    assertArrayEquals(new String[] {"a < 3 and b > 1"}, args);
  }

  @Test
  public void testParseDate() throws Exception {
    Fixture f = new Fixture();
    Object o = f.parse("1/2/2004", Date.class);
    assertEquals(java.util.Date.class, o.getClass());
  }

  public static Parse executeFixture(String[][] table) throws ParseException {
    String pageString = makeFixtureTable(table);
    Parse page = new Parse(pageString);
    Fixture fixture = new Fixture();
    fixture.doTables(page);
    return page;
  }

  @Test
  public void testCanChangeFriendlyExceptions() throws Exception {
    Fixture fixture = new Fixture() {
      public boolean isFriendlyException(Throwable exception) {
        return true;
      }
    };

    Parse cell = new Parse("td", "", null, null);
    fixture.exception(cell, new NullPointerException("gobble gobble"));
    assertSubString("gobble gobble", cell.body);
    assertNotSubString("Exception", cell.body);
  }

  @Test
  public void testClearingSymbols() throws Exception {
    Fixture.setSymbol("blah", "blah");
    assertEquals("blah", Fixture.getSymbol("blah"));

    Fixture.ClearSymbols();
    assertEquals(null, Fixture.getSymbol("blah"));
  }

  private static String makeFixtureTable(String table[][]) {
    StringBuffer buf = new StringBuffer();
    buf.append("<table>\n");
    for (String[] row : table) {
      buf.append("  <tr>");
      for (String cell : row) {
        buf.append("<td>").append(cell).append("</td>");
      }
      buf.append("</tr>\n");
    }
    buf.append("</table>\n");
    return buf.toString();
  }
}

class FixtureOne extends Fixture {
}

class FixtureTwo extends Fixture {
}

class TheThirdFixture extends Fixture {
}
