// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.testutil.RegexTest;

public class WidgetRootTest extends RegexTest
{
	private WikiPage rootPage;

	protected void setUp() throws Exception
	{
		rootPage = InMemoryPage.makeRoot("RooT");
	}

	public void testVariablesOneTheRootPage() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		PageData data = root.getData();
		data.setContent("!define v1 {Variable #1}\n");
		root.commit(data);
		WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "!define v2 {blah}\n${v1}\n");
		data = page.getData();
		assertEquals("Variable #1", data.getVariable("v1"));
	}

	public void testProcessLiterals() throws Exception
	{
		WidgetRoot root = new WidgetRoot("", rootPage);
		assertEquals(0, root.getLiterals().size());
		String result = root.processLiterals("With a !-literal-! in the middle");
		assertNotSubString("!-", result);
		assertEquals(1, root.getLiterals().size());
		assertEquals("literal", root.getLiteral(0));
	}

	public void testProcessLiteralsCalledWhenConstructed() throws Exception
	{
		WidgetRoot root = new WidgetRoot("With !-another literal-! in the middle", rootPage);
		assertEquals(1, root.getLiterals().size());
		assertEquals("another literal", root.getLiteral(0));
	}

	public void testLiteralsInConstructionAndAfterwards() throws Exception
	{
		WidgetRoot root = new WidgetRoot("the !-first-! literal", rootPage);
		String result = root.processLiterals("the !-second-! literal");

		assertEquals("the first literal", root.render());
		assertEquals("the !lit(1) literal", result);
		assertEquals(2, root.getLiterals().size());
		assertEquals("first", root.getLiteral(0));
		assertEquals("second", root.getLiteral(1));
	}
}