// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;

import java.util.*;

//created by Jason Sypher

public class LastModifiedWidgetTest extends WidgetTest
{
	private WikiPage root;
	private WikiPage page;
	private LastModifiedWidget widget;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "some text");
		widget = new LastModifiedWidget(new WidgetRoot(page), "!lastmodified");
	}

	public void testRegularExpression() throws Exception
	{
		assertMatchEquals("!lastmodified", "!lastmodified");
	}

	public void testResults() throws Exception
	{
		setUp();
		Date date = page.getData().getProperties().getLastModificationTime();
		String formattedDate = LastModifiedWidget.formatDate(date);
		assertHasRegexp(formattedDate, widget.render());
	}

	public void testDateFormat() throws Exception
	{
		GregorianCalendar date = new GregorianCalendar(2003, 3, 1, 11, 41, 30);
		String formattedDate = LastModifiedWidget.formatDate(date.getTime());
		assertEquals("Apr 01, 2003 at 11:41:30 AM", formattedDate);
	}

	public void testDefaultUsername() throws Exception
	{
		assertSubString("Last modified anonymously", widget.render());
	}

	public void testUsername() throws Exception
	{
		PageData data = page.getData();
		data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Aladdin");
		page.commit(data);

		assertSubString("Last modified by Aladdin", widget.render());
	}

	protected String getRegexp()
	{
		return LastModifiedWidget.REGEXP;
	}

}
