// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

public class ListWidgetTest extends WidgetTestCase {
  private MockWidgetRoot widgetRoot;

  protected void setUp() throws Exception {
    widgetRoot = new MockWidgetRoot();
  }

  public void testRegexp() throws Exception {
    assertMatchEquals(" *Item1", " *Item1");
    assertMatchEquals(" *Item1\n *Item2", " *Item1\n *Item2");
    assertMatchEquals(" * Item1\n *  Item2\n", " * Item1\n *  Item2\n");
    assertMatchEquals(" *Item1\n  *Item1a", " *Item1\n  *Item1a");
    assertMatchEquals("*Item1  *Item1a", null);
    assertMatchEquals(". *Item1", null);
    assertMatchEquals(" 1Item1", " 1Item1");
    assertMatchEquals("\nWikiTextExample *hello\n", null);
    assertMatch(" * 50 ways to leave your lover.");
  }

  public void testSimpleList() throws Exception {
    formsSimpleOneElementList("Item1");
    formsSimpleOneElementList("50 ways to leave your lover");
  }

  private void formsSimpleOneElementList(String itemText)
    throws Exception {
    ListWidget list = new ListWidget(widgetRoot, " *" + itemText);
    assertTrue("should not be ordered", !list.isOrdered());
    assertEquals(0, list.getLevel());
    assertEquals(1, list.numberOfChildren());
    WikiWidget child = list.nextChild();
    assertEquals(ListItemWidget.class, child.getClass());
    ListItemWidget item = (ListItemWidget) child;
    assertEquals(1, item.numberOfChildren());
    child = item.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals(itemText, ((TextWidget) child).getText());
  }

  public void testSimpleOrderedList() throws Exception {
    ListWidget list = new ListWidget(widgetRoot, " 1Item1");
    assertTrue("should be ordered", list.isOrdered());
    assertEquals(0, list.getLevel());
    assertEquals(1, list.numberOfChildren());
  }

  public void testMultipleItems() throws Exception {
    ListWidget list = new ListWidget(widgetRoot, " *Item1\n *Item2");
    assertEquals(2, list.numberOfChildren());
    assertEquals(ListItemWidget.class, list.nextChild().getClass());
    assertEquals(ListItemWidget.class, list.nextChild().getClass());
  }

  public void testMultiLevelList() throws Exception {
    ListWidget list = new ListWidget(widgetRoot, " *Item1\n  1Item1a\n *Item2");
    assertEquals(2, list.numberOfChildren());
    this.assertNotNull(list.children.get(0));
    assertEquals(0, list.getLevel());
    assertEquals(ListItemWidget.class, list.children.get(0).getClass());
    assertEquals(ListItemWidget.class, list.children.get(1).getClass());
    
    WikiWidget child = ((ListItemWidget)list.getChildren().get(0)).getChildren().get(1);
    assertEquals(ListWidget.class, child.getClass());
    ListWidget childList = (ListWidget) child;
    assertEquals(1, childList.getLevel());
    assertTrue("should be ordered", childList.isOrdered());
  }

  public void testHtml() throws Exception {
    compareHtmlResult(" *Item1", "<ul>\n\t<li>Item1</li>\n</ul>\n");
    compareHtmlResult(" 1Item1", "<ol>\n\t<li>Item1</li>\n</ol>\n");
    compareHtmlResult(" *Item1\n  0Item1a", "<ul>\n\t<li>Item1<ol>\n\t\t<li>Item1a</li>\n</ol>\n</li>\n</ul>\n");
    compareHtmlResult(" * 50 ways to leave your lover", "<ul>\n\t<li>50 ways to leave your lover</li>\n</ul>\n");
  }

  private void compareHtmlResult(String s1, String s2) throws Exception {
    ListWidget list = new ListWidget(widgetRoot, s1);
    assertEquals(s2, list.render());
  }

  protected String getRegexp() {
    return ListWidget.REGEXP;
  }

}
