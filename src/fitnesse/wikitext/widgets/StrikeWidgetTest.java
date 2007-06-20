// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

// created by Jason Sypher

public class StrikeWidgetTest extends WidgetTest
{
	public void testRegularExpression() throws Exception
	{
		assertMatchEquals("abc--123--def", "--123--");
		assertNoMatch("------");
	}

	public void testOutput() throws Exception
	{
		StrikeWidget widget =
			new StrikeWidget(new MockWidgetRoot(), "--some text--");
		assertEquals(1, widget.numberOfChildren());
		WikiWidget child = widget.nextChild();
		assertEquals(TextWidget.class, child.getClass());
		assertEquals("some text", ((TextWidget) child).getText());
		assertEquals("<span class=\"strike\">some text</span>", widget.render());
	}

	public void testEmbeddedDashInStrikedText() throws Exception
	{
		StrikeWidget widget = new StrikeWidget(new MockWidgetRoot(), "--embedded-dash--");
		assertEquals(1, widget.numberOfChildren());
		WikiWidget child = widget.nextChild();
		assertEquals(TextWidget.class, child.getClass());
		assertEquals("embedded-dash", ((TextWidget) child).getText());
		assertEquals("<span class=\"strike\">embedded-dash</span>", widget.render());
	}

	public void testEvilExponentialMatch() throws Exception
	{
		long startTime = System.currentTimeMillis();

		assertNoMatch("--1234567890123456789012");

		long endTime = System.currentTimeMillis();
		assertTrue("took too long", endTime - startTime < 1000);
	}

	public static void main(String[] args)
	{
		junit.textui.TestRunner.run(StrikeWidgetTest.class);
	}

	protected String getRegexp()
	{
		return StrikeWidget.REGEXP;
	}

}
