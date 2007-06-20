// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.WidgetRoot;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

public class WikiTextTranslatorTest extends TestCase
{
	private WikiPage root;
	private WikiPage page;
	private PageCrawler crawler;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.WikiTextTranslatorTest"});
	}

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		page = crawler.addPage(root, PathParser.parse("WidgetRoot"));
	}

	public void tearDown() throws Exception
	{
	}

	public void testTranslation1() throws Exception
	{
		String wikiText = "!c !1 This is a WidgetRoot\n" +
			"\n" +
			"''' ''Some Bold and Italic text'' '''\n";
		String html = "<div class=\"centered\"><h1>This is a <a href=\"WidgetRoot\">WidgetRoot</a></h1></div>" +
			"<br>" +
			"<b> <i>Some Bold and Italic text</i> </b><br>";
		assertEquals(html, translate(wikiText, page));
	}

	public void testHtmlEscape() throws Exception
	{
		String wikiText = "<h1>this \"&\" that</h1>";
		String html = "&lt;h1&gt;this \"&amp;\" that&lt;/h1&gt;";
		assertEquals(html, translate(wikiText, new WikiPageDummy()));
	}

	public void testTableHtml() throws Exception
	{
		String wikiText = "|this|is|a|table|\n|that|has|four|columns|\n";
		String html = "<table border=\"1\" cellspacing=\"0\">\n<tr><td>this</td>" + HtmlElement.endl +
			"<td>is</td>" + HtmlElement.endl +
			"<td>a</td>" + HtmlElement.endl +
			"<td>table</td>" + HtmlElement.endl +
			"</tr>\n" +
			"<tr><td>that</td>" + HtmlElement.endl +
			"<td>has</td>" + HtmlElement.endl +
			"<td>four</td>" + HtmlElement.endl +
			"<td>columns</td>" + HtmlElement.endl +
			"</tr>\n</table>\n";
		assertEquals(html, translate(wikiText, new WikiPageDummy()));
	}

	private static String translate(String value, WikiPage source) throws Exception
	{
		WidgetRoot page = new WidgetRoot(value, source);
		return page.render();
	}
}