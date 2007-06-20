// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPageDummy;

import java.util.regex.*;

public class ClasspathWidgetTest extends WidgetTest
{
	public void testRegexp() throws Exception
	{
		assertMatchEquals("!path somePath", "!path somePath");
	}

	public void testHtml() throws Exception
	{
		ClasspathWidget widget = new ClasspathWidget(new MockWidgetRoot(), "!path some.path");
		Pattern p = Pattern.compile("classpath: some.path");
		Matcher match = p.matcher(widget.render());
		assertTrue("pattern not found", match.find());
	}

	public void testAsWikiText() throws Exception
	{
		final String PATH_WIDGET = "!path some.path";
		ClasspathWidget w = new ClasspathWidget(new MockWidgetRoot(), PATH_WIDGET);
		assertEquals(PATH_WIDGET, w.asWikiText());
	}

	public void testPathWithVariable() throws Exception
	{
		String text = "!define BASE {/my/base/}\n!path ${BASE}*.jar\n";
		WidgetRoot root = new WidgetRoot(text, new WikiPageDummy());
		String html = root.render();
		assertSubString("/my/base/*.jar", html);
	}

	public void testPathWikiTextWithVariable() throws Exception
	{
		String text = "!define BASE {/my/base/}\n!path ${BASE}*.jar\n";
		WidgetRoot root = new WidgetRoot(text, new WikiPageDummy());
		String text2 = root.asWikiText();
		assertSubString("!path ${BASE}*.jar", text2);
	}

	public void testIsWidgetWithTextArgument() throws Exception
	{
		ClasspathWidget widget = new ClasspathWidget(new MockWidgetRoot(), "!path some.path");
		assertTrue(widget instanceof WidgetWithTextArgument);
	}

	protected String getRegexp()
	{
		return ClasspathWidget.REGEXP;
	}
}
