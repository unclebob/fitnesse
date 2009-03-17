// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WikiWidget;

public class ParentWidgetTest extends TestCase {
  private ParentWidget parent;
  private WikiPage rootPage;

  class MockParentWidget extends ParentWidget {
    MockParentWidget(ParentWidget parent) throws Exception {
      super(parent);
    }

    public String render() throws Exception {
      return null;
    }
  }

  public void setUp() throws Exception {
    rootPage = InMemoryPage.makeRoot("RooT");
    parent = new MockParentWidget(new MockWidgetRoot());
  }

  public void tearDown() throws Exception {
  }

  public void testEmptyPage() throws Exception {
    assertEquals(0, parent.numberOfChildren());
  }

  public void testAddOneChild() throws Exception {
    MockWidget mock1 = new MockWidget(parent, "mock1");
    assertEquals(1, parent.numberOfChildren());
    WikiWidget widget = parent.nextChild();
    assertTrue("should be a fitnesse.wikitext.widgets.MockWidget", widget instanceof MockWidget);
    assertSame(mock1, widget);
  }

  public void testAddTwoChildren() throws Exception {
    MockWidget mock1 = new MockWidget(parent, "mock1");
    MockWidget mock2 = new MockWidget(parent, "mock2");

    assertEquals(2, parent.numberOfChildren());
    assertTrue("should have next", parent.hasNextChild());
    assertSame(mock1, parent.nextChild());
    assertSame(mock2, parent.nextChild());

    assertTrue("should not have next", !parent.hasNextChild());
  }

  public void testNextChildWhenThereIsNoNext() throws Exception {
    try {
      parent.nextChild();
      fail("Exception should have been thrown");
    }
    catch (Exception e) {
    }
  }

  public void testChildHtml() throws Exception {
    new MockWidget(parent, "mock1");
    assertEquals("mock1", parent.childHtml());
  }

  public void testChildHtml2() throws Exception {
    new MockWidget(parent, "mock1");
    new MockWidget(parent, "mock2");
    assertEquals("mock1mock2", parent.childHtml());
  }

  public void testVariables() throws Exception {
    ParentWidget root = new WidgetRoot(rootPage);
    ParentWidget parent1 = new MockParentWidget(root);
    ParentWidget parent2 = new MockParentWidget(parent1);
    parent2.addVariable("someKey", "someValue");

    assertEquals("someValue", root.getVariable("someKey"));
    assertEquals("someValue", parent1.getVariable("someKey"));
    assertEquals("someValue", parent2.getVariable("someKey"));
  }

}
