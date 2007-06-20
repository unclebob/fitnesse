// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.util.regex.Pattern;

public class HruleWidgetTest extends TestCase
{
	private WikiPage mockSource;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.HruleWidgetTest"});
	}

	public void setUp() throws Exception
	{
		mockSource = new WikiPageDummy();
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(HruleWidget.REGEXP, "----"));
		assertTrue("match2", Pattern.matches(HruleWidget.REGEXP, "------------------"));
		assertTrue("match3", !Pattern.matches(HruleWidget.REGEXP, "--- -"));
	}

	public void testGetSize() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals(0, widget.size());
		widget = new HruleWidget(new MockWidgetRoot(), "-----");
		assertEquals(1, widget.size());
		widget = new HruleWidget(new MockWidgetRoot(), "--------------");
		assertEquals(10, widget.size());
	}

	public void testHtml() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals("<hr>", widget.render());
		widget = new HruleWidget(new MockWidgetRoot(), "------");
		assertEquals("<hr size=\"3\">", widget.render());
	}
}