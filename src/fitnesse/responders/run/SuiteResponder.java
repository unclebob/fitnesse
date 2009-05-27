// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;


public class SuiteResponder extends TestResponder {

  @Override
  String getTitle() {
    return "Suite Results";
  }

  @Override
  BaseFormatter createXmlFormatter() throws Exception {
    BaseFormatter formatter =  new SuiteXmlFormatter(page, context) {
      @Override
      protected void close() throws Exception {
        closeHtmlResponse();
      }

      @Override
      protected void writeData(byte[] byteArray) throws Exception {
        response.add(byteArray);
      }
    };
    return formatter;
  }

  @Override
  BaseFormatter createHtmlFormatter() throws Exception {
     BaseFormatter formatter =  new SuiteHtmlFormatter(context, page, context.htmlPageFactory) {
      @Override
      protected void writeData(String output) throws Exception {
        addToResponse(output);
      }
      
      @Override
      protected void close() throws Exception {
        closeHtmlResponse(exitCode());
      }      
    };
    return formatter;
  }
  
  @Override
  protected void performExecution() throws Exception {
    SuiteFilter filter = new SuiteFilter(getSuiteTagFilter(), getNotSuiteFilter(), getSuiteFirstTest());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, root, filter);
    MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatter);
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
      startTest = (String)request.getInput("firstTest");
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
