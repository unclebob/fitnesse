// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WikiWidget;

public class VariableDefinitionWidgetTest extends WidgetTestCase {
  public WikiPage root;
  private PageCrawler crawler;
  private ParentWidget widgetRoot;

  protected String getRegexp() {
    return VariableDefinitionWidget.REGEXP;
  }

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertMatch("!define xyz {\n123\r\n456\r\n}");
    assertMatch("!define abc {1}");
    assertMatch("!define abc (1)");
    assertMatch("!define x (!define y {123})");

    assertNoMatch("!define");
    assertNoMatch("!define x");
    assertNoMatch(" !define x {1}");

    //Test allow periods
    assertMatch("!define x.y.z {1}");
    assertMatch("!define .y.z {1}");
    assertMatch("!define x.y. {1}");
    assertMatch("!define .xy. {1}");

    //Paren Literal: Test matches
    assertMatch("!define curly {!-some curly literal-!}");
    assertMatch("!define paren (!-some paren literal-!)");
  }

  public void testHtml() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("MyPage"), "content");
    WikiPage page2 = crawler.addPage(root, PathParser.parse("SecondPage"), "content");

    widgetRoot = new WidgetRoot(page);
    VariableDefinitionWidget widget = new VariableDefinitionWidget(widgetRoot, "!define x {1}\n");
    assertEquals("<span class=\"meta\">variable defined: x=1</span>", widget.render());
    assertEquals("1", widgetRoot.getVariable("x"));

    widgetRoot = new WidgetRoot(page2);
    widget = new VariableDefinitionWidget(widgetRoot, "!define xyzzy (\nbird\n)\n");
    widget.render();
    assertEquals("\nbird\n", widgetRoot.getVariable("xyzzy"));
  }

  public void testRenderedText() throws Exception {
    WikiWidget widget = new VariableDefinitionWidget(new WidgetRoot(root), "!define x (1)\n");
    String renderedText = widget.render();
    assertSubString("x", renderedText);
    assertSubString("1", renderedText);
  }

  //Test includeInto with periods
  public void testRenderedTextWithPeriods() throws Exception {
    WikiWidget widget = new VariableDefinitionWidget(new WidgetRoot(root), "!define x.y.z (1)\n");
    String renderedText = widget.render();
    assertSubString("x.y.z", renderedText);
    assertSubString("1", renderedText);
  }

  public void testDefinePrecedingClasspath() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    String content = "!define SOME_VARIABLE {Variable #1}\n!path c:\\dotnet\\*.dll";
    data.setContent(content);
    root.commit(data);
    assertEquals("Variable #1", data.getVariable("SOME_VARIABLE"));
    assertEquals(1, data.getClasspaths().size());
    assertEquals("c:\\dotnet\\*.dll", data.getClasspaths().get(0));
  }

  public void testNoExtraLineBreakInHtml() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    String content = "!define SOME_VARIABLE {Variable #1}\n!define ANOTHER_VARIABLE {Variable #2}";
    data.setContent(content);
    assertSubString("SOME_VARIABLE=Variable #1</span><br/><span", data.getHtml());
    assertNotSubString("SOME_VARIABLE=Variable #1</span><br/><br/><span", data.getHtml());
  }

  public void testAsWikiText() throws Exception {
    VariableDefinitionWidget widget = new VariableDefinitionWidget(new MockWidgetRoot(), "!define x {1}\n");
    assertEquals("!define x {1}", widget.asWikiText());
    widget = new VariableDefinitionWidget(new MockWidgetRoot(), "!define x ({1})\n");
    assertEquals("!define x ({1})", widget.asWikiText());
  }
}
