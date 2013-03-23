// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.SlimError;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.ParsedPage;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlSlimTestSystem extends SlimTestSystem {
  private HtmlTableScanner tableScanner;

  private Map<String, NodeList> pathToHtmlCache = new HashMap<String, NodeList>();

  public HtmlSlimTestSystem(WikiPage page, Descriptor descriptor, TestSystemListener listener) {
    super(page, descriptor, listener);
  }

  @Override
  protected List<SlimTable> createSlimTables(TestPage pageToTest) {
    NodeList[] fragments = getHtmlFragments(pageToTest);
    tableScanner = new HtmlTableScanner(fragments);
    return createSlimTables(tableScanner);
  }

  private NodeList[] getHtmlFragments(TestPage pageToTest) {
    List<NodeList> fragments = new LinkedList<NodeList>();
    for (WikiPage scenario: pageToTest.getScenarioLibraries()) {
      fragments.add(getHtmlFragment(getPathNameForPage(scenario), pageToTest.decorate(scenario)));
    }
    if (pageToTest.getSetUp() != null) {
      fragments.add(getHtmlFragment(getPathNameForPage(pageToTest.getSetUp()), pageToTest.decorate(pageToTest.getSetUp())));
    }
    if (pageToTest.getSourcePage() != null) {
      fragments.add(makeNodeList(pageToTest.decorate(pageToTest.getSourcePage())));
    }
    if (pageToTest.getTearDown() != null) {
      fragments.add(getHtmlFragment(getPathNameForPage(pageToTest.getTearDown()), pageToTest.decorate(pageToTest.getTearDown())));
    }

    return fragments.toArray(new NodeList[fragments.size()]);
  }

  private NodeList getHtmlFragment(String path, PageData pageData) {
    NodeList nodeList = pathToHtmlCache.get(path);
    if (nodeList == null) {
      nodeList = makeNodeList(pageData);
      pathToHtmlCache.put(path, nodeList);
    }
    return nodeList;
  }

  private NodeList makeNodeList(PageData pageData) {
    String html;ParsedPage parsedPage = pageData.getParsedPage();
    html = parsedPage.toHtml();
    Parser parser = new Parser(new Lexer(new Page(html)));
    try {
      return parser.parse(null);
    } catch (ParserException e) {
      throw new SlimError(e);
    }
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
