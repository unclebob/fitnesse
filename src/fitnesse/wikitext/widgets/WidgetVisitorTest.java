// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetVisitor;
import fitnesse.wikitext.WikiWidget;

public class WidgetVisitorTest extends TestCase implements WidgetVisitor {
  List<WikiWidget> visits = new ArrayList<WikiWidget>();
  private WikiPage root;

  public void visit(WikiWidget widget) {
    visits.add(widget);
  }

  public void visit(WikiWordWidget widget) {
    visits.add(widget);
  }

  public void visit(AliasLinkWidget widget) throws Exception {
  }

  public void setUp() throws Exception {
    visits.clear();
    root = InMemoryPage.makeRoot("RooT");
  }

  public void testSimpleVisitorVisitsAllWidgets() throws Exception {
    ParentWidget root = new WidgetRoot("''hello''", this.root);
    root.acceptVisitor(this);
    assertEquals(3, visits.size());
    assertEquals(WidgetRoot.class, visits.get(0).getClass());
    assertEquals(ItalicWidget.class, visits.get(1).getClass());
    assertEquals(TextWidget.class, visits.get(2).getClass());
  }

  public void testComplexVisitorVisitsAllWidgets() throws Exception {
    ParentWidget root = new WidgetRoot("|CellOne|CellTwo|\n|''hello''|'''hello'''|\n", this.root);
    root.acceptVisitor(this);
    assertEquals(14, visits.size());
  }
}
