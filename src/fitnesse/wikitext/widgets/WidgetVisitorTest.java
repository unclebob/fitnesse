// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.*;
import junit.framework.TestCase;

import java.util.*;

public class WidgetVisitorTest extends TestCase implements WidgetVisitor
{
	List visits = new ArrayList();
	private WikiPage root;

	public void visit(WikiWidget widget)
	{
		visits.add(widget);
	}

	public void visit(WikiWordWidget widget)
	{
		visits.add(widget);
	}

	public void visit(AliasLinkWidget widget) throws Exception
	{
	}

	public void setUp() throws Exception
	{
		visits.clear();
		root = InMemoryPage.makeRoot("RooT");
	}

	public void testSimpleVisitorVisitsAllWidgets() throws Exception
	{
		WidgetRoot root = new WidgetRoot("''hello''", this.root);
		root.acceptVisitor(this);
		assertEquals(3, visits.size());
		assertEquals(WidgetRoot.class, visits.get(0).getClass());
		assertEquals(ItalicWidget.class, visits.get(1).getClass());
		assertEquals(TextWidget.class, visits.get(2).getClass());
	}

	public void testComplexVisitorVisitsAllWidgets() throws Exception
	{
		WidgetRoot root = new WidgetRoot("|CellOne|CellTwo|\n|''hello''|'''hello'''|\n", this.root);
		root.acceptVisitor(this);
		assertEquals(14, visits.size());
	}
}
