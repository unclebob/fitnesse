// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.Response;
import fitnesse.reporting.JavaFormatter;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.WikiImportingResponder;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.CompositeFormatter;
import fitnesse.reporting.PageHistoryFormatter;
import fitnesse.reporting.PageInProgressFormatter;
import fitnesse.reporting.TestHtmlFormatter;
import fitnesse.reporting.TestTextFormatter;
import fitnesse.reporting.XmlFormatter;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.reporting.history.PageHistory;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

public class TestResponder extends ChunkingResponder implements SecureResponder {
  private static final LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected PageData data;
  protected CompositeFormatter formatters;
  protected boolean isInteractive = false;
  private volatile boolean isClosed = false;

  private boolean fastTest = false;
  private boolean remoteDebug = false;
  protected TestSystem testSystem;
  int exitCode;

  public TestResponder() {
    super();
    formatters = new CompositeFormatter();
  }

  protected void doSending() throws Exception {
    checkArguments();
    data = page.getData();

    createFormatters();

    if (isInteractive) {
      makeHtml().render(response.getWriter());
    } else {
      doExecuteTests();
    }

    closeHtmlResponse(exitCode);
  }

  public void doExecuteTests() {
    sendPreTestNotification();

    performExecution();

    exitCode = formatters.getErrorCount();
  }

  private HtmlPage makeHtml() {
    PageCrawler pageCrawler = page.getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath();
    String fullPathName = PathParser.render(fullPath);
    HtmlPage htmlPage = context.pageFactory.newPage();
    htmlPage.setTitle(getTitle() + ": " + fullPathName);
    htmlPage.setPageTitle(new PageTitle(getTitle(), fullPath, data.getAttribute(PageData.PropertySUITES)));
    htmlPage.setNavTemplate("testNav.vm");
    htmlPage.put("actions", new WikiPageActions(page));
    htmlPage.setMainTemplate(mainTemplate());
    htmlPage.put("testExecutor", new TestExecutor());
    htmlPage.setFooterTemplate("wikiFooter.vm");
    htmlPage.put("footerContent", new WikiPageFooterRenderer());
    htmlPage.setErrorNavTemplate("errorNavigator");
    htmlPage.put("errorNavOnDocumentReady", false);

    WikiImportingResponder.handleImportProperties(htmlPage, page);

    return htmlPage;
  }

  public class WikiPageFooterRenderer {
    public String render() {
        return WikiPageUtil.getFooterPageHtml(page);
    }
  }

  public class TestExecutor {
    public void execute() {
        doExecuteTests();
    }
  }

  protected void checkArguments() {
    fastTest |= request.hasInput("debug");
    remoteDebug |= request.hasInput("remote_debug");
  }

  protected void createFormatters() {
    if (response.isXmlFormat()) {
      addXmlFormatter();
    } else if (response.isTextFormat()) {
      addTextFormatter();
    } else if (response.isJavaFormat()) {
      addJavaFormatter();
    } else {
      addHtmlFormatter();
      isInteractive = true;
    }
    if (!request.hasInput("nohistory")) {
      addTestHistoryFormatter();
    }
    addTestInProgressFormatter();
  }

  protected String getTitle() {
    return "Test Results";
  }

  protected String mainTemplate() {
    return "testPage";
  }

  void addXmlFormatter() {
    XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
        return response.getWriter();
      }
    };
    formatters.add(new XmlFormatter(context, page, writerSource));
  }

  void addTextFormatter() {
    formatters.add(new TestTextFormatter(response));
  }
  void addJavaFormatter() {
    formatters.add(JavaFormatter.getInstance(new WikiPagePath(page).toString()));
  }

  void addHtmlFormatter() {
    BaseFormatter formatter = new TestHtmlFormatter(context, page) {
      @Override
      protected void writeData(String output) {
        addToResponse(output);
      }
    };
    formatters.add(formatter);
  }

  protected void addTestHistoryFormatter() {
    HistoryWriterFactory writerFactory = new HistoryWriterFactory();
    formatters.add(new PageHistoryFormatter(context, page, writerFactory));
  }

  protected void addTestInProgressFormatter() {
    formatters.add(new PageInProgressFormatter(context, page));
  }

  protected void sendPreTestNotification() {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void performExecution() {
    List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

    MultipleTestsRunner runner = new MultipleTestsRunner(test2run, context, page, formatters);
    runner.setFastTest(fastTest);
    runner.setDebug(isRemoteDebug());

    if (isEmpty(page))
      formatters.addMessageForBlankHtml();
    runner.executeTestPages();
  }

  private boolean isEmpty(WikiPage page) {
    return page.getData().getContent().length() == 0;
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
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

  public void addToResponse(String output) {
    if (!isClosed()) {
      response.add(output);
    }
  }

  synchronized boolean isClosed() {
    return isClosed;
  }

  synchronized void setClosed() {
    isClosed = true;
  }

  void closeHtmlResponse(int exitCode) {
    if (!isClosed()) {
      setClosed();
      response.closeChunks();
      response.addTrailingHeader("Exit-Code", String.valueOf(exitCode));
      response.closeTrailer();
      response.close();
    }
  }

  void closeHtmlResponse() {
    if (!isClosed()) {
      setClosed();
      response.closeChunks();
      response.close();
    }
  }

  boolean isRemoteDebug() {
    return remoteDebug;
  }

  public Response getResponse() {
    return response;
  }

  public static class HistoryWriterFactory implements XmlFormatter.WriterFactory {
    public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
      File resultPath = new File(PageHistory.makePageHistoryFileName(context, page, counts, time));
      File resultDirectory = new File(resultPath.getParent());
      resultDirectory.mkdirs();
      File resultFile = new File(resultDirectory, resultPath.getName());
      return new PrintWriter(resultFile, "UTF-8");
    }
  }
}
