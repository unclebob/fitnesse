// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.util.regex.Pattern;

public class CenterWidgetTest extends TestCase
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.CenterWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(CenterWidget.REGEXP, "!c centered text\n"));
		assertTrue("match2", Pattern.matches(CenterWidget.REGEXP, "!C more text\n"));
		assertTrue("match3", !Pattern.matches(CenterWidget.REGEXP, "!ctext\n"));
		assertTrue("match4", Pattern.matches(CenterWidget.REGEXP, "!c text\n"));
		assertTrue("match5", !Pattern.matches(CenterWidget.REGEXP, " !c text\n"));
	}

	public void testHtml() throws Exception
	{
		CenterWidget widget = new CenterWidget(new MockWidgetRoot(), "!c some text\n");
		assertEquals("<div class=\"centered\">some text</div>", widget.render());
	}
}