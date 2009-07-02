// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

import junit.framework.TestCase;
import fitnesse.wikitext.WikiWidget;

public class BoldWidgetTest extends TestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertTrue(Pattern.matches(BoldWidget.REGEXP, "'''bold'''"));
    assertTrue(Pattern.matches(BoldWidget.REGEXP, "''''bold''''"));
    assertFalse(Pattern.matches(BoldWidget.REGEXP, "'' 'not bold' ''"));
  }

  public void testBadConstruction() throws Exception {
    BoldWidget widget = new BoldWidget(new MockWidgetRoot(), "''''some text' '''");
    assertEquals(1, widget.numberOfChildren());
    WikiWidget child = widget.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("'some text' ", ((TextWidget) child).getText());
  }

  public void testHtml() throws Exception {
    BoldWidget widget = new BoldWidget(new MockWidgetRoot(), "'''bold text'''");
    assertEquals("<b>bold text</b>", widget.render());
  }

}
