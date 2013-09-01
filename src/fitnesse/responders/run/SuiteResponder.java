// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.CachingSuiteXmlFormatter;
import fitnesse.reporting.PageHistoryFormatter;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.testrunner.MultipleTestsRunner;

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

  @Override
  protected void checkArguments() {
    super.checkArguments();
    includeHtml |= request.hasInput("includehtml");
  }

  @Override
  void addXmlFormatter() {
    CachingSuiteXmlFormatter xmlFormatter = new CachingSuiteXmlFormatter(context, page, response.getWriter());
    if (includeHtml)
      xmlFormatter.includeHtml();
    formatters.add(xmlFormatter);
  }

  @Override
  void addHtmlFormatter() {
    BaseFormatter formatter = new SuiteHtmlFormatter(context, page) {
      protected void writeData(String output) {
        addToResponse(output);
      }
    };
    formatters.add(formatter);
  }

  @Override
  protected void addTestHistoryFormatter() {
    HistoryWriterFactory source = new HistoryWriterFactory();
    formatters.add(new PageHistoryFormatter(context, page, source));
    formatters.add(new SuiteHistoryFormatter(context, page, source));
  }

  @Override
  protected void performExecution() throws IOException, InterruptedException {
    SuiteFilter filter = new SuiteFilter(request, page.getPageCrawler().getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatters);
    runner.setDebug(isRemoteDebug());
    runner.setFastTest(isFastTest());
    runner.executeTestPages();
  }
}
