// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.swingui.TestRunner;

public class LiteralWidgetTest extends WidgetTest
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"LiteralWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testMatches() throws Exception
	{
		assertMatches("!lit(0)");
		assertMatches("!lit(99)");
		assertNoMatch("!lit(-1)");
		assertNoMatch("!lit(a)");
	}

	protected String getRegexp()
	{
		return LiteralWidget.REGEXP;
	}

	public void testWikiWordIsNotParsed() throws Exception
	{
		WidgetRoot root = new MockWidgetRoot();
		root.defineLiteral("Bob");
		LiteralWidget w = new LiteralWidget(root, "!lit(0)");
		String html = w.render();
		assertEquals("Bob", html);
	}
}
