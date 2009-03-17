// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.List;

import junit.framework.TestCase;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wikitext.WidgetBuilder;

public class TextIgnoringWidgetRootTest extends TestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testNoTextWidgetAreCreated() throws Exception {
    String text = "Here is some text with '''bold''' and ''italics''.";
    WikiPageDummy page = new WikiPageDummy("SomePage", text);
    ParentWidget root = new TextIgnoringWidgetRoot(text, page, WidgetBuilder.htmlWidgetBuilder);
    List<?> widgets = root.getChildren();
    assertEquals(2, widgets.size());
    assertTrue(widgets.get(0) instanceof BoldWidget);
    assertTrue(widgets.get(1) instanceof ItalicWidget);
  }

}
