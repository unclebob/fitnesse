// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

//created by Clare McLennan

public class HelpWidgetTest extends WidgetTestCase {
  private WikiPage root;
  private WikiPage page;
  private WikiPage pageNoHelp;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "some text");
    pageNoHelp = root.getPageCrawler().addPage(root, PathParser.parse("NoHelp"), "some text too");
    PageData data = page.getData();
    data.setAttribute(PageData.PropertyHELP, "some page is about some text");
    page.commit(data);
  }

  public void testRegularExpression() throws Exception {
    assertTrue(Pattern.matches(HelpWidget.REGEXP, "!help"));
    assertTrue(Pattern.matches(HelpWidget.REGEXP, "!help -editable"));
  }

  public void testResultsWithHelp() throws Exception {
    setUp();
    HelpWidget widget = new HelpWidget(new WidgetRoot(page), "!help");
    assertEquals("some page is about some text", widget.render());

    HelpWidget editableWidget = new HelpWidget(new WidgetRoot(page), "!help -editable");
    assertEquals("some page is about some text " +
    		"<a href=\"SomePage?properties\">(edit)</a>", editableWidget.render());
  }

  public void testResultsWithoutHelp() throws Exception {
    setUp();
    HelpWidget widget = new HelpWidget(new WidgetRoot(pageNoHelp), "!help");
    assertEquals("", widget.render());

    HelpWidget editableWidget = new HelpWidget(new WidgetRoot(pageNoHelp), "!help -editable");
    assertEquals(" <a href=\"NoHelp?properties\">(edit help text)</a>", editableWidget.render());
  }

  
  protected String getRegexp() {
    return HelpWidget.REGEXP;
  }

}
