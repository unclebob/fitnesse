// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.responders.run.formatters.*;

public class SuiteResponder extends TestResponder {
  private boolean includeHtml;

  @Override
  protected String getTitle() {
    return "Suite Results";
  }

  @Override
  protected String mainTemplate() {
    return "suitePage";
  }

  protected void checkArguments() {
    super.checkArguments();
    includeHtml |= request.hasInput("includehtml");
  }

  void addXmlFormatter() {
    CachingSuiteXmlFormatter xmlFormatter = new CachingSuiteXmlFormatter(context, page, response.getWriter());
    if (includeHtml)
      xmlFormatter.includeHtml();
    formatters.add(xmlFormatter);
  }

  void addHtmlFormatter() {
    BaseFormatter formatter = new SuiteHtmlFormatter(context, page) {
      protected void writeData(String output) {
        addToResponse(output);
      }
    };
    formatters.add(formatter);
  }

  protected void addTestHistoryFormatter() {
    HistoryWriterFactory source = new HistoryWriterFactory();
    formatters.add(new PageHistoryFormatter(context, page, source));
    formatters.add(new SuiteHistoryFormatter(context, page, source));
  }

  protected void performExecution() throws Exception {
    SuiteFilter filter = new SuiteFilter(request, page.getPageCrawler().getFullPath(page).toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatters);
    runner.setDebug(isRemoteDebug());
    runner.setFastTest(isFastTest());
    runner.executeTestPages();
  }
}
