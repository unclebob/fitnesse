// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;

public class AliasLinkWidgetTest extends WidgetTestCase
{
    private WikiPage root;
    private PageCrawler crawler;

    public void setUp() throws Exception
    {
        root = InMemoryPage.makeRoot("RooT");
        crawler = root.getPageCrawler();
    }

	public void tearDown() throws Exception
	{
	}

    public void testMatches() throws Exception
    {
        assertMatches("[[tag][link]]");
        assertMatches("[[this is fun][http://www.objectmentor.com]]");
        assertMatches("[[tag for link with variable][http://${variable}/rest/of/the/link]]");
        assertNoMatch("[[this\nshould][not match]]");
        assertNoMatch("[[][]]");
        assertNoMatch("[[x][]");
        assertNoMatch("[[][x]");
        assertNoMatch("[[x] [x]]");
        assertNoMatch("[[x]]");
      
      assertMatches("[[tag][SomePage#Anchor]]");
      assertMatches("[[tag][http://www.objectmentor.com#jumpTo]]");
      assertMatches("[[tag][  SpacesAllowed  ]]");
      assertMatches("[[tag][#archor${number}]]");
      assertMatches("[[tag][.#archor${number}]]");
      assertMatches("[[tag][SomeLink${= 1 + ${TWO} =}]]");

      assertMatches("[[tag][?query]]");
      assertMatches("[[tag][?query#fragment]]");
      assertMatches("[[tag][SomePage?query#fragment]]");
      assertMatches("[[tag][SomePage.ChildPage?query#fragment]]");
      assertMatches("[[tag][.SomePage.ChildPage?query#fragment]]");
      assertMatches("[[tag][>SomePage.ChildPage?query#fragment]]");
      assertMatches("[[tag][<SomePage.ChildPage?query#fragment]]");
      assertMatches("[[tag][http://www.objectmentor.com?query#fragment]]");
    }

    public void testHtmlAtTopLevelPage() throws Exception
    {
		crawler.addPage(root, PathParser.parse("TestPage"));
		WidgetRoot wroot = new WidgetRoot(new PagePointer(root, PathParser.parse("TestPage")));
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][TestPage]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage\">tag</a>", html);
    }

    public void testHtmlOnSubPage() throws Exception
    {
        crawler.addPage(root, PathParser.parse("ParenT"), "Content");
        WikiPage parent = root.getChildPage("ParenT");
        crawler.addPage(parent, PathParser.parse("ChilD"), "ChilD");
        crawler.addPage(parent, PathParser.parse("ChildTwo"), "ChildTwo");
        WikiPage child = parent.getChildPage("ChilD");
        WidgetRoot parentWidget = new WidgetRoot(new PagePointer(root, PathParser.parse("ParenT.ChilD")));
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[tag][ChildTwo]]");
        assertEquals("<a href=\"ParenT.ChildTwo\">tag</a>", w.render());
        AliasLinkWidget w2 = new AliasLinkWidget(new WidgetRoot(child), "[[tag][.ParenT]]");
        assertEquals("<a href=\"ParenT\">tag</a>", w2.render());
    }

    public void testHtmlForPageThatDoesNotExist() throws Exception
    {
        crawler.addPage(root, PathParser.parse("FrontPage"));
        WidgetRoot parentWidget = new WidgetRoot(new PagePointer(root, PathParser.parse("FrontPage")));
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[tag][TestPage]]");
        assertEquals("tag<a href=\"TestPage?edit\">?</a>", w.render());
    }

    public void testUparrowOnPageThatDoesNotExist() throws Exception
    {
        WikiPage page = crawler.addPage(root, PathParser.parse("FrontPage"));
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(page), "[[tag][^TestPage]]");
        assertEquals("tag<a href=\"FrontPage.TestPage?edit\">?</a>", w.render());
    }

    public void testUparrowOnPageThatDoesExist() throws Exception
    {
        WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
        crawler.addPage(page, PathParser.parse("SubPage"));
        WidgetRoot wroot = new WidgetRoot(page);
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][^SubPage]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage.SubPage\">tag</a>", html);
    }

   //Check >ChildLink if doesn't exist
   public void testRightArrowOnPageThatDoesNotExist() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("FrontPage"));
      AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(page), "[[tag][>TestPage]]");
      assertEquals("tag<a href=\"FrontPage.TestPage?edit\">?</a>", w.render());
   }

   //Check >ChildLink exists 
   public void testRightArrowOnPageThatDoesExist() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      crawler.addPage(page, PathParser.parse("SubPage"));
      WidgetRoot wroot = new WidgetRoot(page);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][>SubPage]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage\">tag</a>", html);
   }

   //Check >ChildLink if doesn't exist
   public void testLeftArrowOnPageThatDoesNotExist() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      WikiPage child2 = crawler.addPage(page, PathParser.parse("SubPage2"));
      AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(child2), "[[tag][<TestPage.SubPage]]");
      assertEquals("tag<a href=\"TestPage.SubPage?edit\">?</a>", w.render());
   }

   //Check <ParentPage.ChildLink 
   public void testLeftArrowOnPageThatDoesExist() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      crawler.addPage(page, PathParser.parse("SubPage"));
      WikiPage child2 = crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child2);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage\">tag</a>", html);
   }

   //Check query suffix 
   public void testLeftArrowOnPageWithQuery() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      crawler.addPage(page, PathParser.parse("SubPage"));
      WikiPage child2 = crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child2);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage?query]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage?query\">tag</a>", html);
   }

   //Check fragment suffix 
   public void testLeftArrowOnPageWithFragment() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      crawler.addPage(page, PathParser.parse("SubPage"));
      WikiPage child2 = crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child2);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage#fragment]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage#fragment\">tag</a>", html);
   }
   
   //Check query suffix plus fragment suffix 
   public void testLeftArrowOnPageWithQueryAndFragment() throws Exception
   {
      WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
      crawler.addPage(page, PathParser.parse("SubPage"));
      WikiPage child2 = crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child2);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage?query#fragment]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage?query#fragment\">tag</a>", html);
   }

   //Check ${var} expansion 
   public void testVariableExpansion() throws Exception
   {
      WikiPage page   = crawler.addPage(root, PathParser.parse("TestPage"));
      WikiPage child1 = crawler.addPage(page, PathParser.parse("SubPage"));
      crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child1);
      wroot.addVariable("TWO", "2");
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage${TWO}]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage2\">tag</a>", html);
   }
   //Check ${= expression =} expansion 
   public void testExpressionExpansion() throws Exception
   {
      WikiPage page   = crawler.addPage(root, PathParser.parse("TestPage"));
      WikiPage child1 = crawler.addPage(page, PathParser.parse("SubPage"));
      crawler.addPage(page, PathParser.parse("SubPage2"));
      WidgetRoot wroot = new WidgetRoot(child1);
      AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][<TestPage.SubPage${= 1 + 1 =}]]");
      String html = w.render();
      assertEquals("<a href=\"TestPage.SubPage2\">tag</a>", html);
   }

    public void testQuestionMarkDoesNotAppear() throws Exception
    {
        WikiPage page = crawler.addPage(root, PathParser.parse("FrontPage"));
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(page), "[[here][http://www.objectmentor.com/FitNesse/fitnesse.zip]]");
		  assertDoesntHaveRegexp("[?]", w.render());
    }
    
    public void testAliasLinkResolvesVariableBeforeBuildingLink() throws Exception
    {
        crawler.addPage(root, PathParser.parse("ParenT"), "!define host {localhost:8080}");
        WikiPage parent = root.getChildPage("ParenT");
        crawler.addPage(parent, PathParser.parse("ChilD"), "ChilD");
        PagePointer childPagePointer = new PagePointer(root, PathParser.parse("ParenT.ChilD"));
        WidgetRoot parentWidget = new WidgetRoot(childPagePointer);
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[tag][http://${host}/something]]");
        assertEquals("<a href=\"http://localhost:8080/something\">tag</a>", w.render());
    }
    
    public void testAliasLinkResolvesBothVariablesBeforeBuildingLink() throws Exception
    {
        crawler.addPage(root, PathParser.parse("ParenT"), "!define host {localhost:8080}\n!define tag {Tag Name}");
        WikiPage parent = root.getChildPage("ParenT");
        crawler.addPage(parent, PathParser.parse("ChilD"), "ChilD");
        PagePointer childPagePointer = new PagePointer(root, PathParser.parse("ParenT.ChilD"));
        WidgetRoot parentWidget = new WidgetRoot(childPagePointer);
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[${tag}][http://${host}/something]]") ;
        assertEquals("<a href=\"http://localhost:8080/something\">Tag Name</a>", w.render());
    }

    protected String getRegexp()
    {
        return AliasLinkWidget.REGEXP;
    }

    public void testUsageOnRootPageDoesntCrash() throws Exception
    {
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), "[[here][PageOne]]");
        try
        {
            w.render();
        }
        catch(Exception e)
        {
            fail("should not throw Exception: " + e);
        }
    }

    public void testAsWikiText() throws Exception
    {
        String ALIAS_LINK = "[[this][that]]";
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), ALIAS_LINK);
        assertEquals(ALIAS_LINK, w.asWikiText());
    }

	public void testLinkToNonExistentWikiPageOnVirtualPage() throws Exception
    {
		// When a virtual page contains a link to a non-existent page, the ? should
		// issue an edit request to the remote machine

        ProxyPage virtualPage = new ProxyPage("VirtualPage", root, "host", 9999, PathParser.parse("RealPage.VirtualPage"));
        AliasLinkWidget widget = new AliasLinkWidget(new WidgetRoot(virtualPage), "[[link][NonExistentPage]]");
        assertEquals("link<a href=\"http://host:9999/RealPage.NonExistentPage?edit\" target=\"NonExistentPage\">?</a>", widget.render());
    }
}
