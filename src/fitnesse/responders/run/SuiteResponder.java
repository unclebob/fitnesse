// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

import fitnesse.http.Request;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.CachingSuiteXmlFormatter;
import fitnesse.testrunner.CompositeFormatter;
import fitnesse.reporting.PageHistoryFormatter;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testsystems.TestSystemListener;

public class SuiteResponder extends TestResponder {
  private static final String NOT_FILTER_ARG = "excludeSuiteFilter";
  private static final String AND_FILTER_ARG = "runTestsMatchingAllTags";
  private static final String OR_FILTER_ARG_1 = "runTestsMatchingAnyTag";
  private static final String OR_FILTER_ARG_2 = "suiteFilter";

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
    SuiteFilter filter = createSuiteFilter(request, page.getPageCrawler().getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    MultipleTestsRunner runner = newMultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite());
    runner.executeTestPages();
  }

  public static SuiteFilter createSuiteFilter(Request request, String suitePath) {
    return new SuiteFilter(getOrTagFilter(request),
            getNotSuiteFilter(request),
            getAndTagFilters(request),
            getSuiteFirstTest(request, suitePath));
  }

  private static String getOrTagFilter(Request request) {
    return request != null ? getOrFilterString(request) : null;
  }

  private static String getOrFilterString(Request request) {
    //request already confirmed not-null
    String orFilterString = null;
    if(request.getInput(OR_FILTER_ARG_1) != null){
      orFilterString = (String) request.getInput(OR_FILTER_ARG_1);
    } else {
      orFilterString = (String) request.getInput(OR_FILTER_ARG_2);
    }
    return orFilterString;
  }

  private static String getNotSuiteFilter(Request request) {
    return request != null ? (String) request.getInput(NOT_FILTER_ARG) : null;
  }

  private static String getAndTagFilters(Request request) {
    return request != null ? (String) request.getInput(AND_FILTER_ARG) : null;
  }


  private static String getSuiteFirstTest(Request request, String suiteName) {
    String startTest = null;
    if (request != null) {
      startTest = (String) request.getInput("firstTest");
    }

    if (startTest != null) {
      if (startTest.indexOf(suiteName) != 0) {
        startTest = suiteName + "." + startTest;
      }
    }

    return startTest;
  }

}
