package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.RawHtml;
import fitnesse.wiki.MockWikiPage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CollapsableWidgetTest extends WidgetTest
{
	public void testRegExp() throws Exception
	{
		assertMatches("!* Some title\n content \n*!");
		assertMatches("!*> Some title\n content \n*!");
		assertMatches("!********** Some title\n content \n**************!");
		assertMatches("!* title\n * list\r*!");

		assertNoMatch("!* title content *!");
		assertNoMatch("!*missing a space\n content \n*!");
		assertNoMatch("!* Some title\n content *!\n");
		assertNoMatch("!* Some title\n content *!...");
	}

	protected String getRegexp()
	{
		return CollapsableWidget.REGEXP;
	}

	public void testRender() throws Exception
	{
		CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!* title\ncontent\n*!");
		String html = widget.render();
		assertSubString("title", html);
		assertSubString("content", html);
		assertSubString("collapsableOpen.gif", html);
	}

	public void testExpandedOrCollapsed() throws Exception
	{
		CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!* title\ncontent\n*!");
		assertTrue(widget.expanded);

		widget = new CollapsableWidget(new MockWidgetRoot(), "!*> title\ncontent\n*!");
		assertFalse(widget.expanded);
	}

	public void testRenderCollapsedSection() throws Exception
	{
		CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(), "!*> title\ncontent\n*!");
		String html = widget.render();
		assertSubString("class=\"hidden\"", html);
		assertNotSubString("class=\"collapsable\"", html);
		assertSubString("collapsableClosed.gif", html);
	}

	public void testTwoCollapsableSections() throws Exception
	{
		String text = "!* section1\nsection1 content\n*!\n" +
		  "!* section2\nsection2 content\n*!\n";
		WidgetRoot widgetRoot = new WidgetRoot(text, new MockWikiPage());
		String html = widgetRoot.render();
		assertSubString("<span class=\"meta\">section1</span>", html);
		assertSubString("<span class=\"meta\">section2</span>", html);
	}

	public void testEatsNewlineAtEnd() throws Exception
	{
		String text = "!* section1\nsection1 content\n*!\n";
		WidgetRoot widgetRoot = new WidgetRoot(text, new MockWikiPage());
		String html = widgetRoot.render();
		assertNotSubString("<br>", html);
	}

	public void testMakeCollapsableSecion() throws Exception
	{
		CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot());
		HtmlTag outerTag = widget.makeCollapsableSection(new RawHtml("title"), new RawHtml("content"));
		assertEquals("div", outerTag.tagName());
		assertEquals("collapse_rim", outerTag.getAttribute("class"));

		List childTags = removeNewlineTags(outerTag);
		HtmlTag anchor = (HtmlTag) childTags.get(0);
		assertEquals("a", anchor.tagName());

		HtmlElement title = (HtmlElement) childTags.get(1);
		assertEquals("title", title.html());

		HtmlTag contentDiv = (HtmlTag) childTags.get(2);
		assertEquals("div", contentDiv.tagName());
		assertEquals("collapsable", contentDiv.getAttribute("class"));

		HtmlElement content = (HtmlElement) removeNewlineTags(contentDiv).get(0);
		assertEquals("content", content.html());
	}

	public void testWeirdBugThatUncleBobEncountered() throws Exception
	{
		try
		{
			new CollapsableWidget(new MockWidgetRoot(), "!* Title\n * list element\n*!\n");
			new CollapsableWidget(new MockWidgetRoot(), "!* Title\n * list element\r\n*!\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("no exception expected." + e.getMessage());
		}
	}

	private List removeNewlineTags(HtmlTag tag) throws Exception
	{
		List childTags = new LinkedList(tag.childTags);
		for(Iterator iterator = childTags.iterator(); iterator.hasNext();)
		{
			HtmlElement element = (HtmlElement) iterator.next();
			if("".equals(element.html().trim()))
				iterator.remove();
		}
		return childTags;
	}

}
