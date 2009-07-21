// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

import java.io.Writer;

public class SuiteResponder extends TestResponder {
  String getTitle() {
    return "Suite Results";
  }

  void addXmlFormatter() throws Exception {
    XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
        return makeResponseWriter();
      }
    };
    formatters.add(new SuiteXmlFormatter(context, page, writerSource));
  }

  void addHtmlFormatter() throws Exception {
    BaseFormatter formatter = new SuiteHtmlFormatter(context, page, context.htmlPageFactory) {
      protected void writeData(String output) throws Exception {
        addToResponse(output);
      }
    };
    formatters.add(formatter);
  }

  protected void addTestHistoryFormatter() throws Exception {
    HistoryWriterFactory source = new HistoryWriterFactory();
    formatters.add(new SuiteXmlFormatter(context, page, source));
    formatters.add(new PageHistoryFormatter(context, page, source));
  }

  protected void performExecution() throws Exception {
    SuiteFilter filter = new SuiteFilter(getSuiteTagFilter(), getNotSuiteFilter(), getSuiteFirstTest());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, root, filter);
    MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatters);
    runner.setDebug(isRemoteDebug());
    runner.executeTestPages();
  }

  private String getSuiteTagFilter() {
    return request != null ? (String) request.getInput("suiteFilter") : null;
  }

  private String getNotSuiteFilter() {
    return request != null ? (String) request.getInput("excludeSuiteFilter") : null;
  }


  private String getSuiteFirstTest() throws Exception {
    String startTest = null;
    if (request != null) {
      startTest = (String) request.getInput("firstTest");
    }

    if (startTest != null) {
      String suiteName = page.getPageCrawler().getFullPath(page).toString();
      if (startTest.indexOf(suiteName) != 0) {
        startTest = suiteName + "." + startTest;
      }
    }

    return startTest;
  }
}
