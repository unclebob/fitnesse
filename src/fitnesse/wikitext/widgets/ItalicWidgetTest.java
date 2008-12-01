// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.RegexTestCase;

public class ItalicWidgetTest extends RegexTestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertMatches(ItalicWidget.REGEXP, "''italic''");
    assertMatches(ItalicWidget.REGEXP, "'' 'italic' ''");
  }

  public void testItalicWidgetRendersHtmlItalics() throws Exception {
    ItalicWidget widget = new ItalicWidget(new MockWidgetRoot(), "''italic text''");
    assertEquals("<i>italic text</i>", widget.render());
  }
}