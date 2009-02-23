// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import org.w3c.dom.Element;

import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class SuiteResponder extends TestResponder implements ResultsListener {

  private SuiteHtmlFormatter suiteFormatter;
  private TestSummary xmlPageCounts = new TestSummary();

  protected void finishSending() throws Exception {
  }

  protected void performExecution() throws Exception {
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, root, getSuiteFilter());
    MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, this);
    runner.executeTestPages();
    if (response.isXmlFormat()) {
      addFinalCounts();
    }
    completeResponse();
  }

  private void addFinalCounts() throws Exception {
    Element finalCounts = testResultsDocument.createElement("finalCounts");
    testResultsElement.appendChild(finalCounts);
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "right", Integer.toString(xmlPageCounts.right));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "wrong", Integer.toString(xmlPageCounts.wrong));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "ignores", Integer.toString(xmlPageCounts.ignores));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "exceptions", Integer.toString(xmlPageCounts.exceptions));
  }

  protected void close() throws Exception {
    response.add(suiteFormatter.testOutput());
    response.add(suiteFormatter.tail());
    response.closeChunks();
    response.addTrailingHeader("Exit-Code", String.valueOf(exitCode()));
    response.closeTrailer();
    response.close();
  }

  public void announceStartNewTest(WikiPage newTest) throws Exception {
    if (response.isHtmlFormat()) {
      PageCrawler pageCrawler = page.getPageCrawler();
      String relativeName = pageCrawler.getRelativeName(page, newTest);
      WikiPagePath fullPath = pageCrawler.getFullPath(newTest);
      String fullPathName = PathParser.render(fullPath);
      suiteFormatter.startOutputForNewTest(relativeName, fullPathName);
    }
  }

  public void processTestResults(WikiPage testPage, TestSummary testSummary) throws Exception {
    PageCrawler pageCrawler = page.getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(page, testPage);
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    if (response.isXmlFormat()) {
      addTestResultsToXmlDocument(testSummary, relativeName);
      xmlPageCounts.tallyPageCounts(testSummary);
    } else {
      assertionCounts.tally(testSummary);
      addToResponse(suiteFormatter.acceptResults(relativeName, testSummary));
    }
  }

  protected void makeFormatter() throws Exception {
    suiteFormatter = new SuiteHtmlFormatter(html);
    formatter = suiteFormatter;
  }

  protected String pageType() {
    return "Suite Results";
  }

  private String getSuiteFilter() {
    return request != null ? (String) request.getInput("suiteFilter") : null;
  }

  @Override
  public void announceStartTestSystem(String testSystemName, String testRunner) throws Exception {
    if (response.isHtmlFormat()) {
      suiteFormatter.announceTestSystem(testSystemName);
      addToResponse(suiteFormatter.getTestSystemHeader(testSystemName  + ":" + testRunner));
    }
  }

  @Override
  public void processTestOutput(String output) throws Exception {
    if (response.isXmlFormat()) {
      super.acceptOutputFirst(output);
    } else if (response.isHtmlFormat()) {
      suiteFormatter.acceptOutput(output);
    }
  }

  @Override
  public void setExecutionLog(CompositeExecutionLog log) {
    this.log = log;
  }
}
