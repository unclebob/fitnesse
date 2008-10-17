// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.components.CommandRunner;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.*;

import java.util.LinkedList;

public class TestResponder extends ChunkingResponder implements TestSystemListener, SecureResponder {
  protected static final String emptyPageContent = "OH NO! This page is empty!";
  protected static final int htmlDepth = 2;

  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();

  protected HtmlPage html;
  protected CommandRunner commandRunner;
  protected ExecutionLog log;
  protected PageData data;
  private boolean closed = false;
  private TestSystem.TestSummary assertionCounts = new TestSystem.TestSummary();
  protected TestHtmlFormatter formatter;
  protected String classPath;
  private String testableHtml;
  protected TestSystem testSystem;

  protected void doSending() throws Exception {
    data = page.getData();
    startHtml();
    sendPreTestNotification();
    testSystem = new FitTestSystem(context, data, this);

    classPath = new ClassPathBuilder().getClasspath(page);
    String className = getClassName(data, request);
    commandRunner = testSystem.start(classPath, className);
    log = new ExecutionLog(page, commandRunner);
    prepareForExecution();

    if (testSystem.isSuccessfullyStarted())
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
    testSystem.send(testableHtml);
    testSystem.bye();
  }

  protected void prepareForExecution() throws Exception {
    addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));

    SetupTeardownIncluder.includeInto(data, true);
    testableHtml = data.getHtml();
    if (testableHtml.length() == 0)
      testableHtml = handleBlankHtml();
  }

  protected void startHtml() throws Exception {
    buildHtml();
    addToResponse(formatter.head());
  }

  protected PageCrawler getPageCrawler() {
    PageCrawler crawler = root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    return crawler;
  }

  public void acceptResults(TestSystem.TestSummary testSummary) throws Exception {
    assertionCounts.tally(testSummary);
  }

  public synchronized void exceptionOccurred(Exception e) {
    try {
      log.addException(e);
      log.addReason("Test execution aborted abnormally with error code " + commandRunner.getExitCode());

      completeResponse();
      testSystem.kill();
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  protected synchronized void completeResponse() throws Exception {
    if (!closed) {
      closed = true;
      log.publish();
      addLogAndClose();
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
    response.addTrailingHeader("Exit-Code", String.valueOf(commandRunner.getExitCode()));
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

  public void acceptOutput(String output) throws Exception {
    response.add(output);
  }

  private String handleBlankHtml() throws Exception {
    response.add(formatter.messageForBlankHtml());
    return emptyPageContent;
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

  public String getClassName(PageData data, Request request) throws Exception {
    //todo No test fails if I replace this with String program = null;
    String program = (String) request.getInput("className");
    if (program == null)
      program = data.getVariable("TEST_RUNNER");
    if (program == null)
      program = "fit.FitServer";
    return program;
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  protected String cssClassFor(Counts count) {
    if (count.wrong > 0)
      return "fail";
    else if (count.exceptions > 0 || count.right + count.ignores == 0)
      return "error";
    else if (count.ignores > 0 && count.right == 0)
      return "ignore";
    else
      return "pass";
  }

  protected int exitCode() {
    return assertionCounts.wrong + assertionCounts.exceptions;
  }

  public static void registerListener(TestEventListener listener) {
    eventListeners.add(listener);
  }
}
