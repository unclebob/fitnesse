// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.RawHtml;
import fitnesse.wiki.WikiPageDummy;

public class CollapsableWidgetTest extends WidgetTestCase {
  public void testRegExp() throws Exception {
    assertMatch("!* Some title\n content \n*!");
    assertMatch("!*> Some title\n content \n*!");
    assertMatch("!********** Some title\n content \n**************!");
    assertMatch("!* title\n * list\r*!");

    assertNoMatch("!* title content *!");
    assertNoMatch("!*missing a space\n content \n*!");
    assertNoMatch("!* Some title\n content *!\n");
    assertNoMatch("!* Some title\n content *!...");

    //invisible: Matches
    assertMatch("!*< Some title\n content \n*!");
    assertMatch("!***< Some title\n content \n***!");
  }

  protected String getRegexp() {
    return CollapsableWidget.REGEXP;
  }

  public void testRender() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!* title\ncontent\n*!");
    String html = widget.render();
    assertSubString("title", html);
    assertSubString("content", html);
    assertSubString("collapsableOpen.gif", html);
    assertSubString("<a href=\"javascript:expandAll();\">Expand All</a>", html);
    assertSubString("<a href=\"javascript:collapseAll();\">Collapse All</a>", html);
  }

  //invisible: Test invisible too
  public void testExpandedOrCollapsedOrInvisible() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!* title\ncontent\n*!");
    assertTrue(widget.expanded);
    assertFalse(widget.invisible);

    widget = new CollapsableWidget(new MockWidgetRoot(), "!*> title\ncontent\n*!");
    assertFalse(widget.expanded);
    assertFalse(widget.invisible);

    //invisible: Test invisible flags
    widget = new CollapsableWidget(new MockWidgetRoot(), "!*< title\ncontent\n*!");
    assertFalse(widget.expanded);
    assertTrue(widget.invisible);
  }

  public void testRenderCollapsedSection() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!*> title\ncontent\n*!");
    String html = widget.render();
    assertSubString("class=\"hidden\"", html);
    assertNotSubString("class=\"collapsable\"", html);
    assertSubString("collapsableClosed.gif", html);
  }

  //invisible: Test invisible class
  public void testRenderInvisibleSection() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!*< title\ncontent\n*!\n");
    String html = widget.render();
    assertSubString("class=\"invisible\"", html);
    assertNotSubString("class=\"collapsable\"", html);
  }

  public void testTwoCollapsableSections() throws Exception {
    String text = "!* section1\nsection1 content\n*!\n" +
      "!* section2\nsection2 content\n*!\n";
    ParentWidget widgetRoot = new WidgetRoot(text, new WikiPageDummy());
    String html = widgetRoot.render();
    assertSubString("<span class=\"meta\">section1", html);
    assertSubString("<span class=\"meta\">section2", html);
  }

  public void testEatsNewlineAtEnd() throws Exception {
    String text = "!* section1\nsection1 content\n*!\n";
    ParentWidget widgetRoot = new WidgetRoot(text, new WikiPageDummy());
    String html = widgetRoot.render();
    assertNotSubString("<br/>", html);
  }

  public void testMakeCollapsableSection() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot());
    HtmlTag outerTag = widget.makeCollapsableSection(new RawHtml("title"), new RawHtml("content"));
    assertEquals("div", outerTag.tagName());
    assertEquals("collapse_rim", outerTag.getAttribute("class"));

    List<?> childTags = removeNewlineTags(outerTag);

    HtmlTag collapseAllLinksDiv = (HtmlTag) childTags.get(0);
    assertEquals("div", collapseAllLinksDiv.tagName());

    HtmlTag anchor = (HtmlTag) childTags.get(1);
    assertEquals("a", anchor.tagName());

    HtmlElement title = (HtmlElement) childTags.get(2);
    assertEquals("title", title.html());

    HtmlTag contentDiv = (HtmlTag) childTags.get(3);
    assertEquals("div", contentDiv.tagName());
    assertEquals("collapsable", contentDiv.getAttribute("class"));

    HtmlElement content = (HtmlElement) removeNewlineTags(contentDiv).get(0);
    assertEquals("content", content.html());
  }

  public void testWeirdBugThatUncleBobEncountered() throws Exception {
    try {
      new CollapsableWidget(new MockWidgetRoot(), "!* Title\n * list element\n*!\n");
      new CollapsableWidget(new MockWidgetRoot(), "!* Title\n * list element\r\n*!\n");
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("no exception expected." + e.getMessage());
    }
  }

  public void testEditLinkSuppressedWhenWidgetBuilderConstructorIsUsed() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!* title\ncontent\n*!");
    String html = widget.render();
    assertDoesntHaveRegexp("^.*href.*edit.*$", html);
  }

  public void testEditLinkIncludedWhenOtherConstructorsAreUsed() throws Exception {
    CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), new MockWidgetRoot(),
      "title", "!* title\ncontent\n*!", "include", false);
    String html = widget.render();
    assertHasRegexp("^.*href.*edit.*$", html);
  }


  private List<?> removeNewlineTags(HtmlTag tag) throws Exception {
    List<?> childTags = new LinkedList<Object>(tag.childTags);
    for (Iterator<?> iterator = childTags.iterator(); iterator.hasNext();) {
      HtmlElement element = (HtmlElement) iterator.next();
      if ("".equals(element.html().trim()))
        iterator.remove();
    }
    return childTags;
  }
}
