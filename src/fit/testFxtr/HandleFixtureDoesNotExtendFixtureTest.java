// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fit.testFxtr;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import util.StringUtil;
import fit.Fixture;
import fit.Parse;

public class HandleFixtureDoesNotExtendFixtureTest extends TestCase {
  public void testLearnHowBadFixtureClassIsHandled() throws Exception {
    List<String> tableLines = Arrays.asList(new String[]
      {"<table>",
        "    <tr>",
        "        <td>fit.testFxtr.WouldBeFixture</td>",
        "    </tr>",
        "</table>"});

    String tableText = StringUtil.join(tableLines, "\r\n");

    Parse tableForFaultyFixture = new Parse(tableText);

    new Fixture().doTables(tableForFaultyFixture);
    String fixtureClassCellText = tableForFaultyFixture.at(0, 0, 0).body;

    assertEquals("fit.testFxtr.WouldBeFixture<hr/> "
      + "<span class=\"fit_label\">"
      + "Class fit.testFxtr.WouldBeFixture is not a fixture." + "</span>",
      fixtureClassCellText);
  }

}
