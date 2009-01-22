// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

public class AnchorDeclarationWidgetTest extends WidgetTestCase {
  public void testRegularExpressionToMatch() throws Exception {
    assertTrue("Match 1", Pattern.matches(AnchorDeclarationWidget.REGEXP, "!anchor name"));
    assertTrue("Match 2", Pattern.matches(AnchorDeclarationWidget.REGEXP, "!anchor 1234"));
    assertFalse("Match 3", Pattern.matches(AnchorDeclarationWidget.REGEXP, "!anchor @#$@#%"));
    assertFalse("Match 4", Pattern.matches(AnchorDeclarationWidget.REGEXP, "! anchor name"));
    assertFalse("Match 5", Pattern.matches(AnchorDeclarationWidget.REGEXP, "!anchor name other stuff"));
    assertFalse("Match 6", Pattern.matches(AnchorDeclarationWidget.REGEXP, "!anchor name "));
  }

  public void testRendering() throws Exception {
    AnchorDeclarationWidget declarationWidget = new AnchorDeclarationWidget(null, "!anchor name");
    assertEquals("<a name=\"name\"> </a>", declarationWidget.render().trim());
  }

  public void testRenderingManyWordMatch() throws Exception {
    AnchorDeclarationWidget declarationWidget = new AnchorDeclarationWidget(null, "!anchor name some other stuff");
    assertEquals("<a name=\"name\"> </a>", declarationWidget.render().trim());
  }

  public void testRenderingTextBefore() throws Exception {
    AnchorDeclarationWidget declarationWidget = new AnchorDeclarationWidget(null, "stuff!anchor name some other stuff");
    assertEquals("<a name=\"name\"> </a>", declarationWidget.render().trim());
  }

  public void testRenderingTextBeforeWithSpace() throws Exception {
    AnchorDeclarationWidget declarationWidget = new AnchorDeclarationWidget(null, "stuff !anchor name some other stuff");
    assertEquals("<a name=\"name\"> </a>", declarationWidget.render().trim());
  }

  protected String getRegexp() {
    return AnchorDeclarationWidget.REGEXP;
  }
}
