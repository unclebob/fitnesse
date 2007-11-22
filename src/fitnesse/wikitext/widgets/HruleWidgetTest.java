// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

import java.util.regex.Pattern;

public class HruleWidgetTest extends TestCase
{
	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(HruleWidget.REGEXP, "----"));
		assertTrue("match2", Pattern.matches(HruleWidget.REGEXP, "------------------"));
		assertTrue("match3", !Pattern.matches(HruleWidget.REGEXP, "--- -"));
	}

	public void testGetSize() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals(0, widget.getExtraDashes());
		widget = new HruleWidget(new MockWidgetRoot(), "-----");
		assertEquals(1, widget.getExtraDashes());
		widget = new HruleWidget(new MockWidgetRoot(), "--------------");
		assertEquals(10, widget.getExtraDashes());
	}

	public void testHtml() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals("<hr/>\n", widget.render());
		widget = new HruleWidget(new MockWidgetRoot(), "------");
		assertEquals("<hr size=\"3\"/>\n", widget.render());
	}
}