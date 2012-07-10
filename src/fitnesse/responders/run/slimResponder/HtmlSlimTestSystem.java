// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSystemListener;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.slimTables.TableScanner;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.ParsedPage;

public class HtmlSlimTestSystem extends SlimTestSystem {
  public HtmlSlimTestSystem(WikiPage page, TestSystemListener listener) {
    super(page, listener);
  }

  protected TableScanner scanTheTables(ReadOnlyPageData pageData) {
    ParsedPage parsedPage = pageData.getParsedPage();
    parsedPage.addToFront(getPreparsedScenarioLibrary());
    String html = parsedPage.toHtml();
    return new HtmlTableScanner(html);
  }

  @Override
  protected String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
    replaceExceptionsWithLinks();
    evaluateTables();
    String exceptionsString = exceptions.toHtml();
    
    Table start = (startWithTable != null) ? startWithTable.getTable() : null;
    Table end = (stopBeforeTable != null) ? stopBeforeTable.getTable() : null;
    String testResultHtml = tableScanner.toHtml(start, end);
    return exceptionsString + testResultHtml;
  }
}
