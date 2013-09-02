// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.CachingSuiteXmlFormatter;
import fitnesse.reporting.CompositeFormatter;
import fitnesse.reporting.PageHistoryFormatter;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.ResultsListener;
import fitnesse.testsystems.TestSystemListener;

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
  BaseFormatter newXmlFormatter() {
    CachingSuiteXmlFormatter xmlFormatter = new CachingSuiteXmlFormatter(context, page, response.getWriter());
    if (includeHtml)
      xmlFormatter.includeHtml();
    return xmlFormatter;
  }

  @Override
  BaseFormatter newHtmlFormatter() {
    return new SuiteHtmlFormatter(context, page) {
      protected void writeData(String output) {
        addToResponse(output);
      }
    };
  }

  @Override
  protected TestSystemListener newTestHistoryFormatter() {
    HistoryWriterFactory source = new HistoryWriterFactory();
    CompositeFormatter f = new CompositeFormatter();
    f.addTestSystemListener(new PageHistoryFormatter(context, page, source));
    f.addTestSystemListener(new SuiteHistoryFormatter(context, page, source));
    return f;
  }

  @Override
  protected void performExecution() throws IOException, InterruptedException {
    SuiteFilter filter = new SuiteFilter(request, page.getPageCrawler().getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    MultipleTestsRunner runner = newMultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite());
    runner.executeTestPages();
  }
}
