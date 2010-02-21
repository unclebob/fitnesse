// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import util.RegexTestCase;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WidgetRootTest {
  private WikiPage rootPage;

  @Before
  public void setUp() throws Exception {
    rootPage = InMemoryPage.makeRoot("RooT");
  }

  //PAGE_NAME: Test
  @Test
  public void testPageNameVariable() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"));
    data = page.getData();
    assertEquals("SomePage", data.getVariable("PAGE_NAME"));
  }

  @Test
  public void testParentPageNameVariable() throws Exception
  {
    PageCrawler crawler = rootPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    final String ROOT_PAGE_NAME = "RootPage";
    WikiPage root = crawler.addPage(rootPage, PathParser.parse(ROOT_PAGE_NAME));
    final String INCLUDED_PAGE_NAME = "IncludedPage";
    WikiPage includedPage = crawler.addPage(rootPage, PathParser.parse(INCLUDED_PAGE_NAME));
    WidgetRoot widgetRoot = new WidgetRoot(root);
    WidgetRoot includedRoot = new WidgetRoot(includedPage,widgetRoot);
    PageData data = includedPage.getData();
    assertEquals(INCLUDED_PAGE_NAME,data.getVariable("PAGE_NAME"));
    assertEquals(INCLUDED_PAGE_NAME, includedRoot.getVariable("PAGE_NAME"));
    assertEquals(ROOT_PAGE_NAME,includedRoot.getVariable("PARENT_NAME"));
  }

  @Test
  public void testParentPageNameVariableWithNoParent() throws Exception
  {
    PageCrawler crawler = rootPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    WikiPage root = crawler.addPage(rootPage, PathParser.parse("RootPage"));
    WikiPage includedPage = crawler.addPage(rootPage, PathParser.parse("IncludedPage"));
    WidgetRoot includedRoot = new WidgetRoot(includedPage);
    assertEquals("IncludedPage", includedRoot.getVariable("PAGE_NAME"));
    // PARENT_NAME returns PAGE_NAME if the page isn't included.
    assertEquals("IncludedPage",includedRoot.getVariable("PARENT_NAME"));
  }

  @Test
  public void testMultipleLevelsOfIncludedPages() throws Exception
  {
    PageCrawler crawler = rootPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    final String ROOT_PAGE_NAME = "RootPage";
    WikiPage root = crawler.addPage(rootPage, PathParser.parse(ROOT_PAGE_NAME));
    final String INCLUDED_PAGE_NAME = "IncludedPage";
    WikiPage includedPage = crawler.addPage(rootPage, PathParser.parse(INCLUDED_PAGE_NAME));
    final String SECOND_LEVEL_INCLUDED_PAGE_NAME = "SecondLevelIncludedPage";
    WikiPage secondLevelIncludedPage  = crawler.addPage(rootPage, PathParser.parse(SECOND_LEVEL_INCLUDED_PAGE_NAME));
    WidgetRoot widgetRoot = new WidgetRoot(root);
    WidgetRoot includedRoot = new WidgetRoot(includedPage,widgetRoot);
    WidgetRoot secondLevelRoot = new WidgetRoot(secondLevelIncludedPage,includedRoot);
    PageData data = secondLevelIncludedPage.getData();
    assertEquals(SECOND_LEVEL_INCLUDED_PAGE_NAME,data.getVariable("PAGE_NAME"));
    assertEquals(SECOND_LEVEL_INCLUDED_PAGE_NAME, secondLevelRoot.getVariable("PAGE_NAME"));
    assertEquals(ROOT_PAGE_NAME,secondLevelRoot.getVariable("PARENT_NAME"));
  }

  @Test
  public void testVariablesOneTheRootPage() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    data.setContent("!define v1 {Variable #1}\n");
    root.commit(data);
    WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "!define v2 {blah}\n${v1}\n");
    data = page.getData();
    assertEquals("Variable #1", data.getVariable("v1"));
  }

  @Test
    public void testVariablesFromSystemProperties() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    System.getProperties().setProperty("widgetRootTestKey", "widgetRootTestValue");
    root.commit(data);
    WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "!define v2 {blah}\n${v1}\n");
    data = page.getData();
    assertEquals("widgetRootTestValue", data.getVariable("widgetRootTestKey"));
  }

  @Test
    public void testProcessLiterals() throws Exception {
    WidgetRoot root = new WidgetRoot("", rootPage);
    assertEquals(0, root.getLiterals().size());
    String result = root.processLiterals("With a !-literal-! in the middle");
    RegexTestCase.assertNotSubString("!-", result);
    assertEquals(1, root.getLiterals().size());
    assertEquals("literal", root.getLiteral(0));
  }

  @Test
    public void testProcessLiteralsCalledWhenConstructed() throws Exception {
    WidgetRoot root = new WidgetRoot("With !-another literal-! in the middle", rootPage);
    assertEquals(1, root.getLiterals().size());
    assertEquals("another literal", root.getLiteral(0));
  }

  @Test
    public void testLiteralsInConstructionAndAfterwards() throws Exception {
    WidgetRoot root = new WidgetRoot("the !-first-! literal", rootPage);
    String result = root.processLiterals("the !-second-! literal");

    assertEquals("the first literal", root.render());
    //Paren Literal: () -> ??
    assertEquals("the !lit?1? literal", result);
    assertEquals(2, root.getLiterals().size());
    assertEquals("first", root.getLiteral(0));
    assertEquals("second", root.getLiteral(1));
  }

  @Test
    public void testShouldHavePortVariableAvailable() throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.port = 9876;
    new FitNesse(context, false);
    WidgetRoot root = new WidgetRoot("", rootPage);
    assertEquals("9876", root.getVariable("FITNESSE_PORT"));
  }

  @Test
    public void testShouldHaveRootPathVariableAvailable() throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.rootPath = "/home/fitnesse";
    new FitNesse(context, false);
    WidgetRoot root = new WidgetRoot("", rootPage);
    assertEquals("/home/fitnesse", root.getVariable("FITNESSE_ROOTPATH"));
  }

  @Test
  public void carriageReturnsShouldNotMatterIfPresentOnPage() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"), "''italics''\r\n\r'''bold'''\r\n\r");
    PageData data = page.getData();
    String html = data.getHtml();
    assertEquals("<i>italics</i><br/><b>bold</b><br/>", html);
  }

  @Test
  public void nestedTableExpansion () throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    data.setContent("!define AA {|aa|\n}\n" +
                    "!define BB (|${AA}|\n)\n"
                   );
    root.commit(data);
    WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "${BB}\n");
    data = page.getData();
    String html = data.getHtml();

    boolean found = Pattern
                    .compile("<table.+<table.+</table.+</table", Pattern.DOTALL)
                    .matcher(html).find();
    assertTrue(html, found);
  }

  @Test
  public void nestedNewlineExpansion () throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    data.setContent("!define LIST {\n * list\n}\n" +
                    "!define BB (|${LIST}|\n)\n"
                   );
    root.commit(data);
    WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "${BB}\n");
    data = page.getData();
    String html = data.getHtml();

    boolean found = Pattern
                    .compile("<table.+<td.+<ul.+<li.+</li.+</ul.+</td.+</table", Pattern.DOTALL)
                    .matcher(html).find();

    assertTrue("[" + html + "]", found);


  }
}
