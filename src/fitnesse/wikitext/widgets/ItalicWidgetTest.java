// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.RegexTest;
import junit.swingui.TestRunner;

public class ItalicWidgetTest extends RegexTest
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.ItalicWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatches(ItalicWidget.REGEXP, "''italic''");
		assertMatches(ItalicWidget.REGEXP, "'' 'italic' ''");
	}

	public void testItalicWidgetRendersHtmlItalics() throws Exception
	{
		ItalicWidget widget = new ItalicWidget(new MockWidgetRoot(), "''italic text''");
		assertEquals("<i>italic text</i>", widget.render());
	}
}