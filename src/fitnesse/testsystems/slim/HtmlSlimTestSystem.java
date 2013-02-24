// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.List;

import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.ParsedPage;

/*
TODO: scan for scenario's on each parent level. Create a TestContext for each page level (linking the parent)
TODO: Ease page loading by parsing the scenario tables only once. figure out what output should look like.
 */
public class HtmlSlimTestSystem extends SlimTestSystem {
  private HtmlTableScanner tableScanner;

  public HtmlSlimTestSystem(WikiPage page, Descriptor descriptor, TestSystemListener listener) {
    super(page, descriptor, listener);
  }

  @Override
  protected List<SlimTable> createSlimTables(TestPage pageToTest) {
    tableScanner = scanTheTables(pageToTest.getDecoratedData());
    return createSlimTables(tableScanner);
  }

  protected HtmlTableScanner scanTheTables(ReadOnlyPageData pageData) {
    ParsedPage parsedPage = pageData.getParsedPage();
    String html = parsedPage.toHtml();
    return new HtmlTableScanner(html);
  }

  @Override
  protected String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
    HtmlTable start = (startWithTable != null) ? (HtmlTable) startWithTable.getTable() : null;
    HtmlTable end = (stopBeforeTable != null) ? (HtmlTable) stopBeforeTable.getTable() : null;
    String testResultHtml = tableScanner.toHtml(start, end);
    return testResultHtml;
  }
}
