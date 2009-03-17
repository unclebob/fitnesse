// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSystemListener;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.TableScanner;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class HtmlSlimTestSystem extends SlimTestSystem {
  public HtmlSlimTestSystem(WikiPage page, TestSystemListener listener) {
    super(page, listener);
  }

  protected TableScanner scanTheTables(PageData pageData) throws Exception {
    return new HtmlTableScanner(pageData.getHtml());
  }

  protected String createHtmlResults() throws Exception {
    replaceExceptionsWithLinks();
    evaluateTables();
    String exceptions = ExceptionList.toHtml(this.exceptions);
    String testResultHtml = tableScanner.toHtml();
    return exceptions + testResultHtml;
  }
}
