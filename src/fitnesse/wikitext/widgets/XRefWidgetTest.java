// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;

public class XRefWidgetTest extends WidgetTestCase
{
	private WikiPage root;
	private WikiPage page;
   private WikiPage child;
	private WidgetRoot wroot, croot;

	public void testRegexp() throws Exception
	{
		assertMatchEquals("!see SomePage", "!see SomePage");
		assertMatchEquals("!see SomePage.SubPage", "!see SomePage.SubPage");
		assertMatchEquals("!see SomePage.SubPage junk", "!see SomePage.SubPage");
      
      //[acd] !see: Child and backward search matching
      assertMatchEquals("!see >SubPage junk", "!see >SubPage");
      assertMatchEquals("!see <SomeParent.SubPage junk", "!see <SomeParent.SubPage");
	}

	protected void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"));
      child    = root.getPageCrawler().addPage(page, PathParser.parse("SomeChild"));
      /*child2*/ root.getPageCrawler().addPage(page, PathParser.parse("SomeChild2"));
      
		wroot = new WidgetRoot(page);
      croot = new WidgetRoot(child);
	}

	public void testHtml() throws Exception
	{
		XRefWidget widget = new XRefWidget(wroot, "!see SomePage");
		assertHasRegexp("<b>See: <a href=.*SomePage</a></b>", widget.render());

		widget = new XRefWidget(wroot, "!see NoPage");
		assertHasRegexp("<b>See: NoPage<a href=.*>?</a></b>", widget.render());

      //[acd] !see: Left & right arrow testing
      widget = new XRefWidget(wroot, "!see >SomeChild");
      assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild.*SomeChild</a></b>", widget.render());
      
      widget = new XRefWidget(croot, "!see <SomePage.SomeChild2");
      assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild2.*SomePage.SomeChild2</a></b>", widget.render());
      
      //[acd] Regracing
      wroot.addVariable(WikiWordWidget.REGRACE_LINK, "true");
      widget = new XRefWidget(wroot, "!see SomePage");
      assertHasRegexp("<b>See: <a href=.*Some Page</a></b>", widget.render());

      widget = new XRefWidget(wroot, "!see NoPage");
      assertHasRegexp("<b>See: NoPage<a href=.*>?</a></b>", widget.render());

      widget = new XRefWidget(wroot, "!see >SomeChild");
      assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild.*&gt;Some Child</a></b>", widget.render());
      
      croot.addVariable(WikiWordWidget.REGRACE_LINK, "true");
      widget = new XRefWidget(croot, "!see <SomePage.SomeChild2");
      assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild2.*&lt;Some Page .Some Child 2</a></b>", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		final String TEST_WIDGET = "!see SomePage";
		XRefWidget w = new XRefWidget(wroot, TEST_WIDGET);
		assertEquals(TEST_WIDGET, w.asWikiText());
	}

	protected String getRegexp()
	{
		return XRefWidget.REGEXP;
	}
}
