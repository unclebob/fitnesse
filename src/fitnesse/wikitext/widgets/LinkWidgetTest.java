// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPageDummy;

public class LinkWidgetTest extends WidgetTestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertMatchEquals("http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html", "http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html");
    assertMatchEquals("http://files/someFile", "http://files/someFile");
    assertMatchEquals("http://files", "http://files");
    assertMatchEquals("http://objectmentor.com", "http://objectmentor.com");
    assertMatchEquals("(http://objectmentor.com)", "http://objectmentor.com");
    assertMatchEquals("http://objectmentor.com.", "http://objectmentor.com");
    assertMatchEquals("(http://objectmentor.com).", "http://objectmentor.com");
    assertMatchEquals("https://objectmentor.com", "https://objectmentor.com");
  }

  public void testHtml() throws Exception {
    LinkWidget widget = new LinkWidget(new MockWidgetRoot(), "http://host.com/file.html");
    assertEquals("<a href=\"http://host.com/file.html\">http://host.com/file.html</a>", widget.render());

    widget = new LinkWidget(new MockWidgetRoot(), "http://files/somePage");
    assertEquals("<a href=\"/files/somePage\">http://files/somePage</a>", widget.render());

    widget = new LinkWidget(new MockWidgetRoot(), "http://www.objectmentor.com");
    assertEquals("<a href=\"http://www.objectmentor.com\">http://www.objectmentor.com</a>", widget.render());
  }

  public void testAsWikiText() throws Exception {
    final String LINK_TEXT = "http://xyz.com";
    LinkWidget widget = new LinkWidget(new MockWidgetRoot(), LINK_TEXT);
    assertEquals(LINK_TEXT, widget.asWikiText());
  }

  public void testHttpsLink() throws Exception {
    String link = "https://link.com";
    LinkWidget widget = new LinkWidget(new MockWidgetRoot(), link);
    assertEquals("<a href=\"https://link.com\">https://link.com</a>", widget.render());
    assertEquals(link, widget.asWikiText());
  }

  public void testLinkWikiWithVariable() throws Exception {
    String text = "!define HOST {somehost}\nhttp://www.${HOST}.com\n";
    ParentWidget root = new WidgetRoot(text, new WikiPageDummy());
    assertSubString("<a href=\"http://www.somehost.com\">http://www.somehost.com</a>", root.render());
    assertEquals(text, root.asWikiText());
  }

  protected String getRegexp() {
    return LinkWidget.REGEXP;
  }
}
