// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.RegexTest;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.Pattern;

public class PreformattedWidgetTest extends RegexTest
{
	public void testRegexp() throws Exception
	{
		Pattern pattern = Pattern.compile(PreformattedWidget.REGEXP, Pattern.DOTALL);
		assertTrue("match1", pattern.matcher("{{{preformatted}}}").matches());
		assertTrue("match2", pattern.matcher("{{{{preformatted}}}}").matches());
		assertFalse("match3", pattern.matcher("{{ {not preformatted}}}").matches());
		assertTrue("match4", pattern.matcher("{{{\npreformatted\n}}}").matches());
	}

	public void testHtml() throws Exception
	{
		PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{preformatted text}}}");
		assertEquals("<pre>preformatted text</pre>", widget.render());
	}

	public void testMultiLine() throws Exception
	{
		PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{\npreformatted text\n}}}");
		assertEquals("<pre>\npreformatted text\n</pre>", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{preformatted text}}}");
		assertEquals("{{{preformatted text}}}", widget.asWikiText());
	}

	public void testThatLiteralsWorkInPreformattedText() throws Exception
	{
		WidgetRoot root = new WidgetRoot("{{{abc !-123-! xyz}}}", new WikiPageDummy(), WidgetBuilder.htmlWidgetBuilder);
		String text = root.render();
		assertEquals("<pre>abc 123 xyz</pre>", text);
	}

	public void testThatVariablesWorkInPreformattedText() throws Exception
	{
		WidgetRoot root = new WidgetRoot("!define X {123}\n{{{abc ${X} xyz}}}", new WikiPageDummy(), WidgetBuilder.htmlWidgetBuilder);
		String text = root.render();
		assertSubString("<pre>abc 123 xyz</pre>", text);
	}
}
