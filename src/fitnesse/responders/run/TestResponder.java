// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.html.TagGroup;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.*;

import java.util.LinkedList;

public class TestResponder extends ChunkingResponder implements TestSystemListener, SecureResponder {
  protected static final int htmlDepth = 2;
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected HtmlPage html;
  protected CompositeExecutionLog log;
  protected PageData data;
  private boolean closed = false;
  private TestSummary assertionCounts = new TestSummary();
  protected TestHtmlFormatter formatter;
  protected TestSystemGroup testSystemGroup;
  protected String classPath;

  protected void doSending() throws Exception {
    data = page.getData();
    classPath = buildClassPath();
    startHtml();
    sendPreTestNotification();

    testSystemGroup = new TestSystemGroup(context, page, this);
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
    String testSystemName = TestSystem.getTestSystemName(data);
    String testRunner = TestSystem.getTestRunner(data);
    TestSystem testSystem = testSystemGroup.startTestSystem(testSystemName, testRunner, classPath);
    if (testSystemGroup.isSuccessfullyStarted()) {
      addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));
      SetupTeardownIncluder.includeInto(data, true);
      if (data.getContent().length() == 0)
        addEmptyContentMessage();
      testSystem.sendPageData(data);
      testSystemGroup.bye();
    }
  }

  protected String buildClassPath() throws Exception {
    return new ClassPathBuilder().getClasspath(page);
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

  public void acceptResults(TestSummary testSummary) throws Exception {
    assertionCounts.tally(testSummary);
  }

  public synchronized void exceptionOccurred(Throwable e) {
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

  public void acceptOutput(String output) throws Exception {
    response.add(output);
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
