// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import util.XmlWriter;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.html.TagGroup;
import fitnesse.responders.ChunkingResponder;
import fitnesse.authentication.SecureResponder;
import fitnesse.responders.WikiImportProperty;
import util.XmlUtil;
import fitnesse.wiki.*;
import fitnesse.FitNesseVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

public class TestResponder extends ChunkingResponder implements TestSystemListener, SecureResponder {
  private static final String PATH_SEPARATOR = System.getProperty("path.separator");
  protected static final int htmlDepth = 2;
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected HtmlPage html;
  protected CompositeExecutionLog log;
  protected PageData data;
  private boolean closed = false;
  protected TestSummary assertionCounts = new TestSummary();
  protected TestHtmlFormatter formatter;
  protected TestSystemGroup testSystemGroup;
  protected String classPath;
  protected Document testResultsDocument;
  protected Element testResultsElement;
  private StringBuffer outputBuffer;
  private boolean fastTest = false;

  protected void doSending() throws Exception {
    fastTest |= request.hasInput("debug");
    data = page.getData();
    classPath = buildClassPath();
    startHtml();
    sendPreTestNotification();

    testSystemGroup = new TestSystemGroup(context, page, this);
    testSystemGroup.setFastTest(fastTest);
    log = testSystemGroup.getExecutionLog();

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
    TestSystem.Descriptor descriptor = TestSystem.getDescriptor(data);
    TestSystem testSystem = testSystemGroup.startTestSystem(descriptor, classPath);
    if (testSystemGroup.isSuccessfullyStarted()) {
      addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));
      SetupTeardownIncluder.includeInto(data, true);
      if (data.getContent().length() == 0)
        addEmptyContentMessage();
      testSystem.runTestsAndGenerateHtml(data);
      testSystemGroup.bye();
    }
  }

  protected String buildClassPath() throws Exception {
    return new ClassPathBuilder().getClasspath(page);
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

  public synchronized void exceptionOccurred(Throwable e) {
    //todo remove sout
    System.err.println("TestResponder.exceptionOcurred:" + e.getMessage());
    try {
      completeResponse();
      testSystemGroup.kill();
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }
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

  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    if (response.isXmlFormat()) {
      addTestResultsToXmlDocument(testSummary, page.getName());
    } else {
      assertionCounts.tally(testSummary);
    }
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

  public void acceptOutputFirst(String output) throws Exception {
    if (response.isXmlFormat()) {
      appendHtmlToBuffer(output);
    } else {
      response.add(output);
    }
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
}
