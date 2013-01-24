// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.ParsedPage;

import java.util.Collections;
import java.util.List;

public class HtmlSlimTestSystem extends SlimTestSystem {
  private ParsedPage preparsedScenarioLibrary;
  private HtmlTableScanner tableScanner;

  public HtmlSlimTestSystem(WikiPage page, Descriptor descriptor, TestSystemListener listener) {
    super(page, descriptor, listener);
  }

  @Override
  protected List<SlimTable> createSlimTables(ReadOnlyPageData pageData) {
    tableScanner = scanTheTables(pageData);
    return createSlimTables(tableScanner);
  }

  protected HtmlTableScanner scanTheTables(ReadOnlyPageData pageData) {
    ParsedPage parsedPage = pageData.getParsedPage();
    parsedPage.addToFront(getPreparsedScenarioLibrary());
    String html = parsedPage.toHtml();
    return new HtmlTableScanner(html);
  }

  public ParsedPage getPreparsedScenarioLibrary() {
    if (preparsedScenarioLibrary == null) {
      preparsedScenarioLibrary = new ParsedPage(page.readOnlyData().getParsedPage(), getScenarioLibraryContent());
    }
    return preparsedScenarioLibrary;
  }


  private String getScenarioLibraryContent() {
    StringBuilder content = new StringBuilder("!*> Precompiled Libraries\n\n");
    content.append(includeUncleLibraries());
    content.append("*!\n");
    return content.toString();
  }

  private String includeUncleLibraries() {
    String content = "";
    List<WikiPage> uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", page);
    Collections.reverse(uncles);
    for (WikiPage uncle : uncles)
      content += include(page.getPageCrawler().getFullPath(uncle));
    return content;
  }

  private String include(WikiPagePath path) {
    return "!include -c ." + path + "\n";
  }

  @Override
  // TODO: Get rid of this
  protected String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
    evaluateTables();
    String exceptionsString = exceptions.toHtml();

    HtmlTable start = (startWithTable != null) ? (HtmlTable) startWithTable.getTable() : null;
    HtmlTable end = (stopBeforeTable != null) ? (HtmlTable) stopBeforeTable.getTable() : null;
    String testResultHtml = tableScanner.toHtml(start, end);
    return exceptionsString + testResultHtml;
  }
}
