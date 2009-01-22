// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

public class TextWidgetTest extends TestCase {
  public void testGetText() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
    assertEquals("some text", widget.getText());
  }

  public void testHtml() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
    assertEquals("some text", widget.render());
  }

  public void testSpecialEscapes() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "text &bang; &bar; &dollar;");
    assertEquals("text ! | $", widget.render());
  }

  public void testAsWikiText() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
    assertEquals("some text", widget.asWikiText());
  }

  public void testSpecialWikiCharsAsWikiText() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "text \\! \\$ \\|");
    assertEquals("text \\! \\$ \\|", widget.asWikiText());
  }

  public void testNewLines() throws Exception {
    TextWidget widget = new TextWidget(new MockWidgetRoot(), "one\ntwo\rthree\r\n");
    assertEquals("one<br/>two<br/>three<br/>", widget.render());    
  }

}
