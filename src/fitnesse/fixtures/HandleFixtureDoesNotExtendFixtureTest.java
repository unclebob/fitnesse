package fitnesse.fixtures;

import java.util.*;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import fit.*;
import fitnesse.util.StringUtil;

public class HandleFixtureDoesNotExtendFixtureTest extends TestCase
{
  public void testLearnHowBadFixtureClassIsHandled() throws Exception
  {
    List tableLines = Arrays.asList(new String[]
    { "<table>",
      "    <tr>",
      "        <td>fitnesse.fixtures.WouldBeFixture</td>",
      "    </tr>",
      "</table>" });

    String tableText = StringUtil.join(tableLines, "\r\n");

    Parse tableForFaultyFixture = new Parse(tableText);

    new Fixture().doTables(tableForFaultyFixture);
    String fixtureClassCellText = tableForFaultyFixture.at(0, 0, 0).body;

    assertEquals("fitnesse.fixtures.WouldBeFixture<hr/> "
        + "<span class=\"fit_label\">"
        + "Class fitnesse.fixtures.WouldBeFixture is not a fixture." + "</span>",
        fixtureClassCellText);
  }

}