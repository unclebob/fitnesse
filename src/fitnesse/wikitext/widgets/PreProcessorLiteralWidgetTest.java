// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

public class PreProcessorLiteralWidgetTest extends WidgetTestCase {
  private ParentWidget root;

  public void setUp() throws Exception {
    root = new MockWidgetRoot();
  }

  protected String getRegexp() {
    return PreProcessorLiteralWidget.REGEXP;
  }

  public void testMatches() throws Exception {
    assertMatch("!-literal-!");
    assertMatch("!-this is a literal-!");
    assertMatch("!-this is\n a literal-!");
    assertMatch("!<this is an escaped literal>!");
    assertMatch("!- !- !-this is a literal-!");
    assertMatch("!-!literal-!");
    assertMatch("!--!");
    assertNoMatch("!-no");
    assertNoMatch("! -no-!");
    assertMatchEquals("!-no-!-!", "!-no-!");
  }

  public void testRender() throws Exception {
    PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!-abc-!");
    assertEquals("!lit?0?", widget.render());
    assertEquals("abc", root.getLiteral(0));
  }

  public void testThatNewlinesShouldBeRenderedAsLinebreakInEscapedLiteral() throws Exception {
    PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!<abc\ndef\nxyz>!");
    assertEquals("!lit?0?", widget.render());
    assertEquals("abc<br/>def<br/>xyz", root.getLiteral(0));
  }

  public void testThatNewLinesShouldNotBeRenderedAsLineBreaksInUnescapedLiteral() throws Exception {
    PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!-abc\ndef\nxyz-!");
    assertEquals("!lit?0?", widget.render());
    assertEquals("abc\ndef\nxyz", root.getLiteral(0));
  }

  public void testEscapedLiteral() throws Exception {
    PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!< <br> >!");
    assertEquals("!lit?0?", widget.render());
    assertEquals(" &lt;br&gt; ", root.getLiteral(0));
  }

  public void testAsWikiText() throws Exception {
    PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!-abc-!");
    assertEquals("!-abc-!", widget.asWikiText());
  }
}
