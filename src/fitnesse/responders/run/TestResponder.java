// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fitnesse.FitNesseVersion;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.XmlWriter;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.responders.WikiImportProperty;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;


public class TestResponder extends ChunkingResponder implements ResultsListener, SecureResponder {
  protected static final int htmlDepth = 2;
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected HtmlPage html;
  protected CompositeExecutionLog log;
  protected PageData data;
  private boolean closed = false;
  protected TestSummary assertionCounts = new TestSummary();
  protected TestHtmlFormatter formatter;
  protected Document testResultsDocument;
  protected Element testResultsElement;
  private StringBuffer outputBuffer;
  MultipleTestsRunner runner = null;
  private boolean fastTest = false;

  protected void doSending() throws Exception {
    fastTest |= request.hasInput("debug");
    data = page.getData();
    startHtml();
    sendPreTestNotification();

    performExecution();

    finishSending();
  }

  private void sendPreTestNotification() throws Exception {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void finishSending() throws Exception {
    completeResponse();
  }

  protected void performExecution() throws Exception {
    if (page.getData().getContent().length() == 0) {
      addEmptyContentMessage();
    }
    
    List<WikiPage> test2run = new SuiteContentsFinder(page, root, null).makePageListForSingleTest();
    
    synchronized (this) {
      runner = new MultipleTestsRunner(test2run, context, page, this);
      runner.setFastTest(fastTest);
    }
    
    runner.executeTestPages();
  }

  protected void startHtml() throws Exception {
    if (response.isXmlFormat()) {
      testResultsDocument = XmlUtil.newDocument();
      testResultsElement = testResultsDocument.createElement("testResults");
      testResultsDocument.appendChild(testResultsElement);
      XmlUtil.addTextNode(testResultsDocument, testResultsElement, "FitNesseVersion", new FitNesseVersion().toString());
      XmlUtil.addTextNode(testResultsDocument, testResultsElement, "rootPath", page.getName());
    } else {
      buildHtml();
      addToResponse(formatter.head());
    }
  }

  protected PageCrawler getPageCrawler() {
    PageCrawler crawler = root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    return crawler;
  }

  protected synchronized void completeResponse() throws Exception {
    if (!closed) {
      closed = true;
      if (response.isXmlFormat()) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(os);
        writer.write(testResultsDocument);
        writer.close();
        response.add(os.toByteArray());
        response.closeChunks();
        response.close();
      } else {
        log.publish();
        addLogAndClose();
      }
    }
  }

  protected final void addLogAndClose() throws Exception {
    addLog();
    close();
  }

  protected void close() throws Exception {
    response.add(HtmlUtil.getHtmlOfInheritedPage("PageFooter", page));
    response.add(formatter.tail());
    response.closeChunks();
    response.addTrailingHeader("Exit-Code", String.valueOf(exitCode()));
    response.closeTrailer();
    response.close();
  }

  protected void addLog() throws Exception {
    response.add(formatter.testSummary(assertionCounts));
    response.add(formatter.executionStatus(log));
  }

  public void addToResponse(String output) throws Exception {
    if (!closed)
      response.add(output);
  }

  protected void addTestResultsToXmlDocument(TestSummary testSummary, String pageName) throws Exception {
    Element resultElement = testResultsDocument.createElement("result");
    testResultsElement.appendChild(resultElement);
    addCountsToResult(testSummary, resultElement);

    XmlUtil.addCdataNode(testResultsDocument, resultElement, "content", outputBuffer.toString());
    outputBuffer = null;

    XmlUtil.addTextNode(testResultsDocument, resultElement, "relativePageName", pageName);
  }

  private void addCountsToResult(TestSummary testSummary, Element resultElement) {
    Element counts = testResultsDocument.createElement("counts");
    resultElement.appendChild(counts);
    XmlUtil.addTextNode(testResultsDocument, counts, "right", Integer.toString(testSummary.right));
    XmlUtil.addTextNode(testResultsDocument, counts, "wrong", Integer.toString(testSummary.wrong));
    XmlUtil.addTextNode(testResultsDocument, counts, "ignores", Integer.toString(testSummary.ignores));
    XmlUtil.addTextNode(testResultsDocument, counts, "exceptions", Integer.toString(testSummary.exceptions));
  }

  private void appendHtmlToBuffer(String output) {
    if (outputBuffer == null) {
      outputBuffer = new StringBuffer();
    }
    outputBuffer.append(output);
  }

  private void addEmptyContentMessage() throws Exception {
    response.add(formatter.messageForBlankHtml());
  }

  protected void buildHtml() throws Exception {
    PageCrawler pageCrawler = page.getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(page);
    String fullPathName = PathParser.render(fullPath);
    html = context.htmlPageFactory.newPage();
    html.title.use(pageType() + ": " + fullPathName);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(fullPathName, pageType()));
    html.actions.use(HtmlUtil.makeActions(data));
    WikiImportProperty.handleImportProperties(html, page, data);

    makeFormatter();
  }

  protected void makeFormatter() throws Exception {
    formatter = new TestHtmlFormatter(html);
  }

  protected String pageType() {
    return "Test Results";
  }

  protected String title() throws Exception {
    WikiPagePath fullPath = getPageCrawler().getFullPath(page);
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.makeLink(PathParser.render(fullPath), page.getName()));
    group.add(HtmlUtil.makeItalic(pageType()));
    return group.html();
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  protected int exitCode() {
    return assertionCounts.wrong + assertionCounts.exceptions;
  }

  public static void registerListener(TestEventListener listener) {
    eventListeners.add(listener);
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public boolean isFastTest() {
    return fastTest;
  }

  @Override
  public void announceStartNewTest(WikiPage test) throws Exception {
    addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));
  }

  @Override
  public void announceStartTestSystem(String testSystemName, String testRunner)
      throws Exception {
  }

  @Override
  public void processTestOutput(String output) throws Exception {
    if (response.isXmlFormat()) {
      appendHtmlToBuffer(output);
    } else {
      response.add(output);
    }
  }

  @Override
  public void processTestResults(WikiPage test, TestSummary testSummary)
      throws Exception {
    if (response.isXmlFormat()) {
      addTestResultsToXmlDocument(testSummary, test.getName());
    } else {
      assertionCounts.tally(testSummary);
    }
  }

  @Override
  public void setExecutionLog(CompositeExecutionLog log) {
    this.log = log;
  }
  
  @Override
  public void errorOccured() {
    try {
      completeResponse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
