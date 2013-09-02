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
import fitnesse.reporting.InteractiveFormatter;
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
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

public class TestResponder extends ChunkingResponder implements SecureResponder {
  // TODO: move this to FitNesseContext
  private static final LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();

  private PageData data;
  private CompositeFormatter formatters;
  private BaseFormatter mainFormatter;
  private volatile boolean isClosed = false;

  private boolean fastTest = false;
  private boolean remoteDebug = false;
  int exitCode;

  public TestResponder() {
    super();
    formatters = new CompositeFormatter();
  }

  private boolean isInteractive() {
    return mainFormatter instanceof InteractiveFormatter;
  }

  protected void doSending() throws Exception {
    checkArguments();
    data = page.getData();

    createFormatters();

    if (isInteractive()) {
      makeHtml().render(response.getWriter());
    } else {
      doExecuteTests();
    }

    closeHtmlResponse(exitCode);
  }

  public void doExecuteTests() {
    sendPreTestNotification();

    try {
      performExecution();
    } catch (Exception e) {
      mainFormatter.errorOccurred(e);
    }

    exitCode = mainFormatter.getErrorCount();
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
      mainFormatter = newXmlFormatter();
    } else if (response.isTextFormat()) {
      mainFormatter = newTextFormatter();
    } else if (response.isJavaFormat()) {
      mainFormatter = newJavaFormatter();
    } else {
      mainFormatter = newHtmlFormatter();
    }
    formatters.addTestSystemListener(mainFormatter);
    if (!request.hasInput("nohistory")) {
      formatters.addTestSystemListener(newTestHistoryFormatter());
    }
    formatters.addTestSystemListener(newTestInProgressFormatter());
  }

  protected String getTitle() {
    return "Test Results";
  }

  protected String mainTemplate() {
    return "testPage";
  }

  BaseFormatter newXmlFormatter() {
    XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
        return response.getWriter();
      }
    };
    return new XmlFormatter(context, page, writerSource);
  }

  BaseFormatter newTextFormatter() {
    return new TestTextFormatter(response);
  }

  BaseFormatter newJavaFormatter() {
    return JavaFormatter.getInstance(new WikiPagePath(page).toString());
  }

  BaseFormatter newHtmlFormatter() {
    return new TestHtmlFormatter(context, page) {
      @Override
      protected void writeData(String output) {
        addToResponse(output);
      }
    };
  }

  protected TestSystemListener newTestHistoryFormatter() {
    HistoryWriterFactory writerFactory = new HistoryWriterFactory();
    return new PageHistoryFormatter(context, page, writerFactory);
  }

  protected TestSystemListener newTestInProgressFormatter() {
    return new PageInProgressFormatter(context);
  }

  protected void sendPreTestNotification() {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void performExecution() throws IOException, InterruptedException {
    List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

    MultipleTestsRunner runner = newMultipleTestsRunner(test2run);

    if (isEmpty(page))
      mainFormatter.addMessageForBlankHtml();
    runner.executeTestPages();
  }

  protected MultipleTestsRunner newMultipleTestsRunner(List<WikiPage> pages) {
    MultipleTestsRunner runner = new MultipleTestsRunner(pages, context, page, formatters);
    runner.setFastTest(fastTest);
    runner.setDebug(remoteDebug);
    return runner;
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
