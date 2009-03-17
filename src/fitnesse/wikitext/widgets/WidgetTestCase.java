// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.RegexTestCase;

public abstract class WidgetTestCase extends RegexTestCase {
  protected void assertMatchEquals(String value, String expected) {
    Matcher match = Pattern.compile(getRegexp(), Pattern.DOTALL | Pattern.MULTILINE).matcher(value);
    if (expected != null) {
      assertTrue("pattern not found in: " + value, match.find());
      assertEquals(expected, match.group());
    } else {
      boolean found = match.find();
      assertTrue((found ? match.group() : "nothing") + " was found in: " + value, !found);
    }
  }

  protected void assertMatch(String s) {
    assertMatchEquals(s, s);
  }

  protected void assertNoMatch(String s) {
    assertMatchEquals(s, null);
  }

  protected abstract String getRegexp();
}
