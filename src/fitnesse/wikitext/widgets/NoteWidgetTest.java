// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

public class NoteWidgetTest extends WidgetTestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertMatchEquals("!note some note", "!note some note");
    assertMatchEquals("! note some note", null);
  }

  public void test() throws Exception {
    WikiWidget widget = new NoteWidget(new MockWidgetRoot(), "!note some note");
    assertEquals("<span class=\"note\">some note</span>", widget.render());
  }

  protected String getRegexp() {
    return NoteWidget.REGEXP;
  }
}
