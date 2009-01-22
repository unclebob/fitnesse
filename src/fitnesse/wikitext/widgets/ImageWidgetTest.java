// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class ImageWidgetTest extends WidgetTestCase {
  public void testRegexp() throws Exception {
    assertMatchEquals("http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.gif", "http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.gif");
    assertMatchEquals("http://www.objectmentor.com/x.jpg", "http://www.objectmentor.com/x.jpg");
    assertMatchEquals("http://www.objectmentor.com/x.GIF", "http://www.objectmentor.com/x.GIF");
    assertMatchEquals("http://www.objectmentor.com/x.JPG", "http://www.objectmentor.com/x.JPG");
    assertMatchEquals("!img http://files/someImage", "!img http://files/someImage");
    assertMatchEquals("!img http://www.oma.com/x.gif", "!img http://www.oma.com/x.gif");
    assertMatchEquals("!img /root/file.gif", "!img /root/file.gif");
    assertMatchEquals("!img  /root/file.gif", null);
    assertMatchEquals("!img-l http://files/x.gif", "!img-l http://files/x.gif");
  }

  public void testWidget() throws Exception {
    ImageWidget widget = new ImageWidget(new MockWidgetRoot(), "http://host.com/file.jpg");
    assertEquals("<img src=\"http://host.com/file.jpg\"/>", widget.render());

    widget = new ImageWidget(new MockWidgetRoot(), "!img http://files/file.jpg");
    assertEquals("<img src=\"/files/file.jpg\"/>", widget.render());

    widget = new ImageWidget(new MockWidgetRoot(), "!img-l http://files/file.jpg");
    assertEquals("<img src=\"/files/file.jpg\" class=\"left\"/>", widget.render());

    widget = new ImageWidget(new MockWidgetRoot(), "!img /files/file.jpg");
    assertEquals("<img src=\"/files/file.jpg\"/>", widget.render());
  }

  public void testAsWikiText() throws Exception {
    checkWikiTextReconstruction("!img http://hello.jpg");
    checkWikiTextReconstruction("!img http://files/hello.jpg");
    checkWikiTextReconstruction("!img-l http://hello.jpg");
    checkWikiTextReconstruction("!img-r http://hello.jpg");
    checkWikiTextReconstruction("http://hello.jpg");
  }

  private void checkWikiTextReconstruction(String original) throws Exception {
    WikiPage root = InMemoryPage.makeRoot("root");
    WikiPage somePage = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"));
    ParentWidget widgetRoot = new WidgetRoot(somePage);
    ImageWidget widget = new ImageWidget(widgetRoot, original);
    assertEquals(original, widget.asWikiText());
  }

  protected String getRegexp() {
    return ImageWidget.REGEXP;
  }
}
