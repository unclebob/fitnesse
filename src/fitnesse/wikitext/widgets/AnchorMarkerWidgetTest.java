// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

public class AnchorMarkerWidgetTest extends WidgetTestCase {
  public void testRegularExpression() throws Exception {
    assertTrue("Match 1", Pattern.matches(AnchorMarkerWidget.REGEXP, ".#anchorName"));
    assertFalse("Match 2", Pattern.matches(AnchorMarkerWidget.REGEXP, ".# anchorName"));
    assertFalse("Match 3", Pattern.matches(AnchorMarkerWidget.REGEXP, ".#anchor Name"));
    assertFalse("Match 4", Pattern.matches(AnchorMarkerWidget.REGEXP, "blah.#anchorName"));
  }

  public void testRender() throws Exception {
    AnchorMarkerWidget widget = new AnchorMarkerWidget(null, ".#anchorName");
    assertEquals("<a href=\"#anchorName\">.#anchorName</a>", widget.render().trim());
  }

  public void testRenderWithSpaces() throws Exception {
    AnchorMarkerWidget widget = new AnchorMarkerWidget(null, ".#anchorName and some other stuff");
    assertEquals("<a href=\"#anchorName\">.#anchorName</a>", widget.render().trim());
  }

  public void testRenderBefore() throws Exception {
    AnchorMarkerWidget widget = new AnchorMarkerWidget(null, "stuffbefore.#anchorName and some other stuff");
    assertEquals("<a href=\"#anchorName\">.#anchorName</a>", widget.render().trim());
  }

  public void testRenderWithNewline() throws Exception {
    AnchorMarkerWidget widget = new AnchorMarkerWidget(null, "stuffbefore\r\n.#anchorName and some other stuff");
    assertEquals("<a href=\"#anchorName\">.#anchorName</a>", widget.render().trim());
  }

  protected String getRegexp() {
    return AnchorMarkerWidget.REGEXP;
  }
}
