// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

import junit.framework.TestCase;
import fitnesse.wiki.WikiPageDummy;

public class CommentWidgetTest extends TestCase {
  private ParentWidget root;

  public void setUp() throws Exception {
    WikiPageDummy page = new WikiPageDummy();
    root = new WidgetRoot(page);
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertTrue("match1", Pattern.matches(CommentWidget.REGEXP, "# Comment text\n"));
    assertTrue("match2", Pattern.matches(CommentWidget.REGEXP, "#\n"));
    assertTrue("match3", !Pattern.matches(CommentWidget.REGEXP, " #\n"));
  }

  public void testHtml() throws Exception {
    CommentWidget widget = new CommentWidget(root, "# some text\n");
    assertEquals("", widget.render());
  }

  public void testAsWikiText() throws Exception {
    CommentWidget widget = new CommentWidget(root, "# some text\n");
    assertEquals("# some text\n", widget.asWikiText());
  }
}
