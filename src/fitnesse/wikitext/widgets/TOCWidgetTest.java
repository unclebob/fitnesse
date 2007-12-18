// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

public class TOCWidgetTest extends WidgetTestCase
{
	private WikiPage root;
	private WikiPage parent, parent2, child1P2, child2P2;
	private PageCrawler crawler;
	private String endl = HtmlElement.endl;

	//===================================================[ SetUp / TearDown
	//
	@Override
   public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		parent = crawler.addPage(root, PathParser.parse("ParenT"), "parent");
		crawler.addPage(root, PathParser.parse("ParentTwo"), "parent two");

      crawler.addPage(parent, PathParser.parse("ChildOne"), "content");
		crawler.addPage(parent, PathParser.parse("ChildTwo"), "content");
		//Regracing
      parent2 = crawler.addPage(root, PathParser.parse("ParenT2"), "parent2");
      child1P2 = crawler.addPage(parent2, PathParser.parse("Child1Page"), "content");
      child2P2 = crawler.addPage(parent2, PathParser.parse("Child2Page"), "content");
	}

	@Override
   public void tearDown() throws Exception
	{
	}

	//===================================================[ Miscellaneous
	//
	@Override
   protected String getRegexp()
	{
		return TOCWidget.REGEXP;
	}

	//===================================================[ Matchers
	//
	public void testMatch() throws Exception
	{
		assertMatchEquals("!contents\n", "!contents");
		assertMatchEquals("!contents -R\n", "!contents -R");
		assertMatchEquals("!contents\r", "!contents");
		assertMatchEquals("!contents -R\r", "!contents -R");
		assertMatchEquals(" !contents\n", null);
		assertMatchEquals(" !contents -R\n", null);
		assertMatchEquals("!contents zap\n", null);
		assertMatchEquals("!contents \n", "!contents ");
      // -R[0-9]...
      assertMatchEquals("!contents -R0\n", "!contents -R0");
      assertMatchEquals("!contents -R1\n", "!contents -R1");
      assertMatchEquals("!contents -R99\n", "!contents -R99");
      assertMatchEquals("!contents -Rx\n", null);
      
      // Regracing
      assertMatchEquals("!contents -g\n", "!contents -g");
      assertMatchEquals("!contents -R -g\n", "!contents -R -g");
      assertMatchEquals("!contents -g\r", "!contents -g");
      assertMatchEquals("!contents -R -g\r", "!contents -R -g");
      assertMatchEquals(" !contents    -g\n", null);
      assertMatchEquals(" !contents -R -g\n", null);
      assertMatchEquals("!contents -gx\n", null);
      assertMatchEquals("!contents -g \n", "!contents -g ");

      // Property suffix
      assertMatchEquals("!contents -p\n", "!contents -p");
      assertMatchEquals("!contents -R -p\n", "!contents -R -p");
      assertMatchEquals("!contents -p\r", "!contents -p");
      assertMatchEquals("!contents -R -p\r", "!contents -R -p");
      assertMatchEquals("!contents -p \n", "!contents -p ");
      assertMatchEquals("!contents -g -p\n", "!contents -g -p");
      assertMatchEquals("!contents  -R2  -g  -p  \n", "!contents  -R2  -g  -p  ");
      assertMatchEquals("!contents -p -g\n", "!contents -p -g");
      assertMatchEquals("!contents -R -p -g\n", "!contents -R -p -g");
      assertMatchEquals(" !contents    -p\n", null);
      assertMatchEquals(" !contents -R -p\n", null);
      assertMatchEquals("!contents -px\n",    null);

      // Filter suffix
      assertMatchEquals("!contents -f\n", "!contents -f");
      assertMatchEquals("!contents -R -f\n", "!contents -R -f");
      assertMatchEquals("!contents -f \n", "!contents -f ");
      assertMatchEquals("!contents -g -p -f\n", "!contents -g -p -f");
      assertMatchEquals("!contents -f -p -g\n", "!contents -f -p -g");
      assertMatchEquals("!contents -R -p -g -f\n", "!contents -R -p -g -f");
      assertMatchEquals("!contents -fx\n", null);

      // Help suffix
      assertMatchEquals("!contents -h\n", "!contents -h");
      assertMatchEquals("!contents -R -h\n", "!contents -R -h");
      assertMatchEquals("!contents -h \n", "!contents -h ");
      assertMatchEquals("!contents -g -p -h\n", "!contents -g -p -h");
      assertMatchEquals("!contents -h -p -g\n", "!contents -h -p -g");
      assertMatchEquals("!contents -R -p -g -f -h\n", "!contents -R -p -g -f -h");
      assertMatchEquals("!contents -hx\n", null);
	}

	//===================================================[ Structural Testing
	// The tests in this section deal solely with top-level and multi-level
	// structures produced by !contents.
	//
	public void testTocOnRoot() throws Exception
	{
		TOCWidget widget = new TOCWidget(new WidgetRoot(root), "!contents\n");
		String html = widget.render();
		assertHasRegexp("ParenT", html);
		assertHasRegexp("ParentTwo", html);
	}

	public void testNoGrandchildren() throws Exception
	{
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithNoHierarchy(), renderHierarchicalTOCWidget());
	}

	public void testWithGrandchildren() throws Exception
	{
		addGrandChild(parent, "ChildOne");
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithGrandChild(), renderHierarchicalTOCWidget());
	}

	public void testWithGreatGrandchildren() throws Exception
	{
		addGrandChild(parent, "ChildOne");
		addGreatGrandChild(parent, "ChildOne");
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithGreatGrandChild(), renderHierarchicalTOCWidget());
	}
   
	public void testIsNotHierarchical() throws Exception
	{
		assertFalse(new TOCWidget(new WidgetRoot(parent), "!contents\n").isRecursive());
	}

	public void testIsHierarchical() throws Exception
	{
		assertTrue(new TOCWidget(new WidgetRoot(parent), "!contents -R\n").isRecursive());
	}

	private WikiPage addGrandChild(WikiPage parent, String childName)
	throws Exception
	{
	   crawler.addPage(parent.getChildPage(childName), PathParser.parse("GrandChild"), "content");
	   return parent.getChildPage(childName).getChildPage("GrandChild");
	}

	private WikiPage addGreatGrandChild(WikiPage parent, String childName)
	throws Exception
	{
	   crawler.addPage(parent.getChildPage(childName).getChildPage("GrandChild"), PathParser.parse("GreatGrandChild"), "content");
	   return parent.getChildPage(childName).getChildPage("GrandChild").getChildPage("GreatGrandChild");
	}

	//--------------------------------------[ Renderers for Hierarchy
	//
	private String renderNormalTOCWidget()
	throws Exception
	{
		return new TOCWidget(new WidgetRoot(parent), "!contents\n").render();
	}

	private String getHtmlWithNoHierarchy()
	{
		return
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
	}

	//--  --  --  --  --  --  --  --  --  --
	private String renderHierarchicalTOCWidget()
	throws Exception
	{
		return new TOCWidget(new WidgetRoot(parent), "!contents -R\n").render();
	}

	private String getHtmlWithGrandChild()
	{
		return
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t\t<div class=\"toc2\">" + endl +
				"\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>" + endl +
				"\t\t\t\t\t</li>" + endl +
				"\t\t\t\t</ul>" + endl +
				"\t\t\t</div>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
	}

	private String getHtmlWithGreatGrandChild()
	{
		String expected =
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t\t<div class=\"toc2\">" + endl +
				"\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>" + endl +
				"\t\t\t\t\t\t<div class=\"toc3\">" + endl +
				"\t\t\t\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild.GreatGrandChild\">GreatGrandChild</a>" + endl +
				"\t\t\t\t\t\t\t\t</li>" + endl +
				"\t\t\t\t\t\t\t</ul>" + endl +
				"\t\t\t\t\t\t</div>" + endl +
				"\t\t\t\t\t</li>" + endl +
				"\t\t\t\t</ul>" + endl +
				"\t\t\t</div>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
		return expected;
	}

	//===================================================[ Virtual Children
	//
	public void testDisplaysVirtualChildren() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("VirtualParent"));
		PageData data = page.getData();
		data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:" + FitNesseUtil.port + "/ParenT");
		page.commit(data);
		try
		{
			FitNesseUtil.startFitnesse(root);
			TOCWidget widget = new TOCWidget(new WidgetRoot(page), "!contents\n");
			String html = widget.render();
			assertEquals(virtualChildrenHtml(), html);
		}
		finally
		{
			FitNesseUtil.stopFitnesse();
		}
	}

	//--------------------------------------[ Renderers for Virtual Children
	//
	//--  --  --  --  --  --  --  --  --  --
	private String virtualChildrenHtml()
	{
		return "<div class=\"toc1\">" + endl +
			"\t<ul>" + endl +
			"\t\t<li>" + endl +
			"\t\t\t<a href=\"VirtualParent.ChildOne\">" + endl +
			"\t\t\t\t<i>ChildOne</i>" + endl +
			"\t\t\t</a>" + endl +
			"\t\t</li>" + endl +
			"\t\t<li>" + endl +
			"\t\t\t<a href=\"VirtualParent.ChildTwo\">" + endl +
			"\t\t\t\t<i>ChildTwo</i>" + endl +
			"\t\t\t</a>" + endl +
			"\t\t</li>" + endl +
			"\t</ul>" + endl +
			"</div>" + endl;

	}

	//===================================================[ Graceful Naming
	//
   public void testWithGreatGrandchildrenRegraced() throws Exception
   {
      addGrandChild(parent2, "Child1Page");
      addGreatGrandChild(parent2, "Child1Page");
      assertEquals(getHtmlWithNoHierarchyRegraced(), renderNormalRegracedTOCWidget());
      assertEquals(getHtmlWithGreatGrandChildRegraced(), renderHierarchicalRegracedTOCWidgetByVar());
      assertEquals(getHtmlWithGreatGrandChildRegraced(), renderHierarchicalRegracedTOCWidgetByOption());
   }

	//--------------------------------------[ Renderers for Regracing
	//
   private String renderNormalRegracedTOCWidget()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	root.addVariable(TOCWidget.REGRACE_TOC, "true");
   	return new TOCWidget(root, "!contents\n").render();
   }

   private String getHtmlWithNoHierarchyRegraced()
   {
      return
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page</a>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
   }

	//--  --  --  --  --  --  --  --  --  --
   private String renderHierarchicalRegracedTOCWidgetByVar()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	root.addVariable(TOCWidget.REGRACE_TOC, "true");
   	return new TOCWidget(root, "!contents -R\n").render();
   }

   private String renderHierarchicalRegracedTOCWidgetByOption()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	return new TOCWidget(root, "!contents -R -g\n").render();
   }

   private String getHtmlWithGreatGrandChildRegraced()  //Regracing
   {
      String expected =
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page</a>" + endl +
            "\t\t\t<div class=\"toc2\">" + endl +
            "\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child</a>" + endl +
            "\t\t\t\t\t\t<div class=\"toc3\">" + endl +
            "\t\t\t\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child</a>" + endl +
            "\t\t\t\t\t\t\t\t</li>" + endl +
            "\t\t\t\t\t\t\t</ul>" + endl +
            "\t\t\t\t\t\t</div>" + endl +
            "\t\t\t\t\t</li>" + endl +
            "\t\t\t\t</ul>" + endl +
            "\t\t\t</div>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
      return expected;
   }

	//===================================================[ Properties with Graceful Naming
	//
   public void testWithGreatGrandchildrenRegracedProp() throws Exception
   {
   	setProperties(child1P2, new String[]{"Suite","Prune"});
   	setProperties(child2P2, new String[]{"Suite","Test","WikiImport"});
      setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Test"});
      setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suite","Test"});
      
      assertEquals(getHtmlWithNoHierarchyRegracedProp(), renderNormalRegracedPropTOCWidget());
      assertEquals(getHtmlWithGreatGrandChildRegracedProp(), renderHierarchicalRegracedPropTOCWidgetByVar());
      assertEquals(getHtmlWithGreatGrandChildRegracedProp(), renderHierarchicalRegracedPropTOCWidgetByOption());

      parent2.getData().addVariable(TOCWidget.PROPERTY_CHARACTERS, "#!%");
      assertEquals(getHtmlWithGreatGrandChildRegracedPropAlt(), renderHierarchicalRegracedPropAltTOCWidget());
   }

   private void setProperties (WikiPage page, String[] propList) throws Exception
   {
   	PageData data = page.getData();
   	WikiPageProperties props = data.getProperties();
   	for (int i = 0;  i < propList.length;  i++)
   	{
   		String[] parts = propList[i].split("=");
   		if (parts.length == 1) props.set(parts[0]); else props.set(parts[0], parts[1]);
   	}
   	
    	page.commit(data);
   }

	//--------------------------------------[ Renderers for Properties with Graceful Names
	//
   private String renderNormalRegracedPropTOCWidget()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	root.addVariable(TOCWidget.REGRACE_TOC, "true");
   	root.addVariable(TOCWidget.PROPERTY_TOC, "true");
   	return new TOCWidget(root, "!contents\n").render();
   }

   private String getHtmlWithNoHierarchyRegracedProp()
   {
      return
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page *-</a>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page *+@</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
   }

	//--  --  --  --  --  --  --  --  --  --
   private String renderHierarchicalRegracedPropTOCWidgetByVar()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	root.addVariable(TOCWidget.REGRACE_TOC, "true");
   	root.addVariable(TOCWidget.PROPERTY_TOC, "true");
   	return new TOCWidget(root, "!contents -R\n").render();
   }

   private String renderHierarchicalRegracedPropTOCWidgetByOption()
   throws Exception
   {
   	WidgetRoot root = new WidgetRoot(parent2);
   	root.addVariable(TOCWidget.REGRACE_TOC, "true");
   	return new TOCWidget(root, "!contents -R -p\n").render();
   }

   private String getHtmlWithGreatGrandChildRegracedProp()
   {
      String expected =
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page *-</a>" + endl +
            "\t\t\t<div class=\"toc2\">" + endl +
            "\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child +</a>" + endl +
            "\t\t\t\t\t\t<div class=\"toc3\">" + endl +
            "\t\t\t\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child *+</a>" + endl +
            "\t\t\t\t\t\t\t\t</li>" + endl +
            "\t\t\t\t\t\t\t</ul>" + endl +
            "\t\t\t\t\t\t</div>" + endl +
            "\t\t\t\t\t</li>" + endl +
            "\t\t\t\t</ul>" + endl +
            "\t\t\t</div>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page *+@</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
      return expected;
   }

	//--  --  --  --  --  --  --  --  --  --
	private String renderHierarchicalRegracedPropAltTOCWidget()
	throws Exception
	{
	WidgetRoot root = new WidgetRoot(parent2);
	root.addVariable(TOCWidget.REGRACE_TOC, "true");
	root.addVariable(TOCWidget.PROPERTY_CHARACTERS, "#!%");
	return new TOCWidget(root, "!contents -R -p\n").render();
	}
	
   private String getHtmlWithGreatGrandChildRegracedPropAlt()
   {
      String expected =
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page #-</a>" + endl +
            "\t\t\t<div class=\"toc2\">" + endl +
            "\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child !</a>" + endl +
            "\t\t\t\t\t\t<div class=\"toc3\">" + endl +
            "\t\t\t\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child #!</a>" + endl +
            "\t\t\t\t\t\t\t\t</li>" + endl +
            "\t\t\t\t\t\t\t</ul>" + endl +
            "\t\t\t\t\t\t</div>" + endl +
            "\t\t\t\t\t</li>" + endl +
            "\t\t\t\t</ul>" + endl +
            "\t\t\t</div>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page #!%</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
      return expected;
   }

   //===================================================[ Filter Suffix
	//
   public void testWithGreatGrandchildrenAndFilters() throws Exception
   {
   	setProperties(child1P2, new String[]{"Suites=F1"});
   	setProperties(child2P2, new String[]{"Suites=F1,F2"});
      setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2"});
      setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2,F3"});
      
      assertEquals(getHtmlWithNoHierarchyFilters(), renderNormalFiltersTOCWidget());
      assertEquals(getHtmlWithGreatGrandChildFilters(), renderHierarchicalFiltersTOCWidgetByVar());
      assertEquals(getHtmlWithGreatGrandChildFilters(), renderHierarchicalFiltersTOCWidgetByOption());
   }

	//--------------------------------------[ Renderers for Properties with Graceful Names
	//
	private String renderNormalFiltersTOCWidget()
   throws Exception
	{
	   WidgetRoot root = new WidgetRoot(parent2);
	   root.addVariable(TOCWidget.FILTER_TOC, "true");
	   return new TOCWidget(root, "!contents -g\n").render();
	}
	
   private String getHtmlWithNoHierarchyFilters()
   {
      return
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
   }

	//--  --  --  --  --  --  --  --  --  --
	private String renderHierarchicalFiltersTOCWidgetByVar()
	throws Exception
	{
		WidgetRoot root = new WidgetRoot(parent2);
		root.addVariable(TOCWidget.FILTER_TOC, "true");
		return new TOCWidget(root, "!contents -R -g\n").render();
	}

	private String renderHierarchicalFiltersTOCWidgetByOption()
	throws Exception
	{
		WidgetRoot root = new WidgetRoot(parent2);
		return new TOCWidget(root, "!contents -R -g -f\n").render();
	}

   private String getHtmlWithGreatGrandChildFilters()
   {
      String expected =
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a>" + endl +
            "\t\t\t<div class=\"toc2\">" + endl +
            "\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child (F2)</a>" + endl +
            "\t\t\t\t\t\t<div class=\"toc3\">" + endl +
            "\t\t\t\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child (F2,F3)</a>" + endl +
            "\t\t\t\t\t\t\t\t</li>" + endl +
            "\t\t\t\t\t\t\t</ul>" + endl +
            "\t\t\t\t\t\t</div>" + endl +
            "\t\t\t\t\t</li>" + endl +
            "\t\t\t\t</ul>" + endl +
            "\t\t\t</div>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
      return expected;
   }

   //===================================================[ Help Suffix
	//
   public void testWithGreatGrandchildrenAndHelp() throws Exception
   {
   	setProperties(child1P2, new String[]{"Suites=F1", "Help=Root child 1 help"});
   	setProperties(child2P2, new String[]{"Suites=F1,F2", "Help=Root child 2 help"});
      setProperties(addGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2", "Help=Grand child help"});
      setProperties(addGreatGrandChild(parent2, "Child1Page"), new String[]{"Suites=F2,F3", "Help=Great grand child help"});
      
      assertEquals(getHtmlWithNoHierarchyHelp(), renderNormalHelpTOCWidget());
      assertEquals(getHtmlWithGreatGrandChildHelp(), renderHierarchicalHelpTOCWidgetByVar());
      assertEquals(getHtmlWithGreatGrandChildHelp(), renderHierarchicalHelpTOCWidgetByOption());
   }

	//--------------------------------------[ Renderers for Properties with Graceful Names
	//
	private String renderNormalHelpTOCWidget()
   throws Exception
	{
	   WidgetRoot root = new WidgetRoot(parent2);
	   return new TOCWidget(root, "!contents -g\n").render();
	}
	
   private String getHtmlWithNoHierarchyHelp()
   {
      return
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\" title=\"Root child 1 help\">Child 1 Page</a>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\" title=\"Root child 2 help\">Child 2 Page</a>" + endl +
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
   }

	//--  --  --  --  --  --  --  --  --  --
	private String renderHierarchicalHelpTOCWidgetByVar()
	throws Exception
	{
		WidgetRoot root = new WidgetRoot(parent2);
		root.addVariable(TOCWidget.HELP_TOC, "true");
		return new TOCWidget(root, "!contents -R -g -f\n").render();
	}

	private String renderHierarchicalHelpTOCWidgetByOption()
	throws Exception
	{
		WidgetRoot root = new WidgetRoot(parent2);
		return new TOCWidget(root, "!contents -R -g -f -h\n").render();
	}

   private String getHtmlWithGreatGrandChildHelp()
   {
   	String hsep = TOCWidget.HELP_PREFIX_DEFAULT;
      String expected =
         "<div class=\"toc1\">" + endl +
            "\t<ul>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child1Page\">Child 1 Page (F1)</a><span class=\"pageHelp\">" + hsep + "Root child 1 help</span>" + endl + 
            "\t\t\t<div class=\"toc2\">" + endl +
            "\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild\">Grand Child (F2)</a><span class=\"pageHelp\">" + hsep + "Grand child help</span>" + endl + 
            "\t\t\t\t\t\t<div class=\"toc3\">" + endl +
            "\t\t\t\t\t\t\t<ul>" + endl +
            "\t\t\t\t\t\t\t\t<li>" + endl +
            "\t\t\t\t\t\t\t\t\t<a href=\"ParenT2.Child1Page.GrandChild.GreatGrandChild\">Great Grand Child (F2,F3)</a><span class=\"pageHelp\">" + hsep + "Great grand child help</span>" + endl + 
            "\t\t\t\t\t\t\t\t</li>" + endl +
            "\t\t\t\t\t\t\t</ul>" + endl +
            "\t\t\t\t\t\t</div>" + endl +
            "\t\t\t\t\t</li>" + endl +
            "\t\t\t\t</ul>" + endl +
            "\t\t\t</div>" + endl +
            "\t\t</li>" + endl +
            "\t\t<li>" + endl +
            "\t\t\t<a href=\"ParenT2.Child2Page\">Child 2 Page (F1,F2)</a><span class=\"pageHelp\">" + hsep + "Root child 2 help</span>" + endl + 
            "\t\t</li>" + endl +
            "\t</ul>" + endl +
            "</div>" + endl;
      return expected;
   }
}
