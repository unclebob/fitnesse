// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class MetaWidgetTest extends WidgetTest
{
	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatches(MetaWidget.REGEXP, "!meta some string");
		assertMatches(MetaWidget.REGEXP, "!meta '''BoldWikiWord'''");
	}

	public void testItalicWidgetRendersHtmlItalics() throws Exception
	{
		MetaWidget widget = new MetaWidget(new MockWidgetRoot(), "!meta text");
		assertEquals("<span class=\"meta\">text</span>", widget.render());
	}

	protected String getRegexp()
	{
		return MetaWidget.REGEXP;
	}

}
