// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.ParsedPage;

/*
TODO: scan for scenario's on each parent level. Create a TestContext for each page level (linking the parent)
TODO: Ease page loading by parsing the scenario tables only once. figure out what output should look like.
 */
public class HtmlSlimTestSystem extends SlimTestSystem {
  private HtmlTableScanner tableScanner;

  private Map<String, String> pathToHtmlCache = new HashMap<String, String>();

  public HtmlSlimTestSystem(WikiPage page, Descriptor descriptor, TestSystemListener listener) {
    super(page, descriptor, listener);
  }

  @Override
  protected List<SlimTable> createSlimTables(TestPage pageToTest) {
    String[] fragments = getHtmlFragments(pageToTest);
    tableScanner = new HtmlTableScanner(fragments);
    return createSlimTables(tableScanner);
  }

  private String[] getHtmlFragments(TestPage pageToTest) {
    List<String> fragments = new LinkedList<String>();
    for (WikiPage scenario: pageToTest.getScenarioLibraries()) {
      fragments.add(getHtmlFragment(getPathNameForPage(scenario), pageToTest.decorate(scenario)));
    }
    if (pageToTest.getSetUp() != null) {
      fragments.add(getHtmlFragment(getPathNameForPage(pageToTest.getSetUp()), pageToTest.decorate(pageToTest.getSetUp())));
    }
    if (pageToTest.getSourcePage() != null) {
      fragments.add(renderPageData(pageToTest.decorate(pageToTest.getSourcePage())));
    }
    if (pageToTest.getTearDown() != null) {
      fragments.add(getHtmlFragment(getPathNameForPage(pageToTest.getTearDown()), pageToTest.decorate(pageToTest.getTearDown())));
    }

    return fragments.toArray(new String[fragments.size()]);
  }

  private String getHtmlFragment(String path, PageData pageData) {
    String html = pathToHtmlCache.get(path);
    if (html == null) {
      html = renderPageData(pageData);
      pathToHtmlCache.put(path, html);
    }
    return html;
  }

  private String renderPageData(PageData pageData) {
    String html;ParsedPage parsedPage = pageData.getParsedPage();
    html = parsedPage.toHtml();
    return html;
  }

  private String getPathNameForPage(WikiPage page) {
    WikiPagePath pagePath = page.getPageCrawler().getFullPath(page);
    return PathParser.render(pagePath);
  }

  @Override
  protected String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
    HtmlTable start = (startWithTable != null) ? (HtmlTable) startWithTable.getTable() : null;
    HtmlTable end = (stopBeforeTable != null) ? (HtmlTable) stopBeforeTable.getTable() : null;
    String testResultHtml = tableScanner.toHtml(start, end);
    return testResultHtml;
  }
}
