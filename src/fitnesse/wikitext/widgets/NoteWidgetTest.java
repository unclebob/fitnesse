// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import junit.swingui.TestRunner;

public class NoteWidgetTest extends WidgetTest
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"NoteTestClass"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatchEquals("!note some note", "!note some note");
		assertMatchEquals("! note some note", null);
	}

	public void test() throws Exception
	{
		WikiWidget widget = new NoteWidget(new MockWidgetRoot(), "!note some note");
		assertEquals("<span class=\"note\">some note</span>", widget.render());
	}

	protected String getRegexp()
	{
		return NoteWidget.REGEXP;
	}
}
