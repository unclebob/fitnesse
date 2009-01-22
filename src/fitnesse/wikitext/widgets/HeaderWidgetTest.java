// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

import java.util.regex.Pattern;

public class HeaderWidgetTest extends TestCase {
  public void testRegexp() throws Exception {
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!1 some text\n"));
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!2 \n"));
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!3 text\n"));
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!4 text\n"));
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!5 text\n"));
    assertTrue(Pattern.matches(HeaderWidget.REGEXP, "!6 text\n"));
    assertTrue(!Pattern.matches(HeaderWidget.REGEXP, "!3text\n"));
    assertTrue(!Pattern.matches(HeaderWidget.REGEXP, "!0 text\n"));
    assertTrue(!Pattern.matches(HeaderWidget.REGEXP, "!7 text\n"));
  }

  public void testGetSize() throws Exception {
    for (int i = 1; i <= 6; i++) {
      HeaderWidget widget = new HeaderWidget(new MockWidgetRoot(), "!" + i + " text \n");
      assertEquals(i, widget.size());
    }
  }

  public void testHtml() throws Exception {
    for (int i = 1; i <= 6; i++) {
      HeaderWidget widget = new HeaderWidget(new MockWidgetRoot(), "!" + i + " some text \n");
      assertEquals("<h" + i + ">some text</h" + i + ">", widget.render());
    }
  }
}
