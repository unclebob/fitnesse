// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.TraversalListener;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Response;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.InteractiveFormatter;
import fitnesse.reporting.PageInProgressFormatter;
import fitnesse.reporting.TestHtmlFormatter;
import fitnesse.reporting.TestTextFormatter;
import fitnesse.reporting.history.TestXmlFormatter;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.WikiImporter;
import fitnesse.responders.WikiImportingResponder;
import fitnesse.responders.WikiImportingTraverser;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

import static fitnesse.responders.WikiImportingTraverser.ImportError;
import static fitnesse.wiki.WikiImportProperty.isAutoUpdated;

public class TestResponder extends ChunkingResponder implements SecureResponder {
  public static final String TEST_RESULT_FILE_DATE_PATTERN = "yyyyMMddHHmmss";
  // TODO: move this to FitNesseContext
  private final WikiImporter wikiImporter;

  private PageData data;
  private BaseFormatter mainFormatter;
  private volatile boolean isClosed = false;

  private boolean debug = false;
  private boolean remoteDebug = false;
  int exitCode;

  public TestResponder() {
    this(new WikiImporter());
  }

  public TestResponder(WikiImporter wikiImporter) {
    super();
    this.wikiImporter = wikiImporter;
  }

  private boolean isInteractive() {
    return mainFormatter instanceof InteractiveFormatter;
  }

  protected void doSending() throws Exception {
    checkArguments();
    data = page.getData();

    createMainFormatter();

    if (isInteractive()) {
      makeHtml().render(response.getWriter());
    } else {
      doExecuteTests();
    }

    closeHtmlResponse(exitCode);
  }

  public void doExecuteTests() {
    if (WikiImportProperty.isImported(data)) {
      importWikiPages();
    }

    try {
      performExecution();
    } catch (Exception e) {
      mainFormatter.errorOccurred(e);
    }

    exitCode = mainFormatter.getErrorCount();
  }

  public void importWikiPages() {
    if (response.isXmlFormat() || !isAutoUpdated(data != null ? data : page.getData())) {
      return;
    }

    try {
      addToResponse("<span class=\"meta\">Updating imported content...</span><span class=\"meta\">");
      new WikiImportingTraverser(wikiImporter, page).traverse(new TraversalListener<Object>() {
        @Override
        public void process(Object pageOrError) {
          if (pageOrError instanceof ImportError) {
            ImportError error = (ImportError) pageOrError;
            addToResponse(" " + error.toString() + ".");
          }
        }
      });
      addToResponse(" Done.");
      // Refresh data, since it might have changed.
      data = page.getData();
    } catch (IOException e) {
      addToResponse(" Import failed: " + e.toString() + ".");
    } finally {
      addToResponse("</span>");
    }
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

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
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
    debug |= request.hasInput("debug");
    remoteDebug |= request.hasInput("remote_debug");
  }

  protected void addFormatters(MultipleTestsRunner runner) {
    runner.addTestSystemListener(mainFormatter);
    if (!request.hasInput("nohistory")) {
      runner.addTestSystemListener(newTestHistoryFormatter());
    }
    runner.addTestSystemListener(newTestInProgressFormatter());
  }

  private void createMainFormatter() {
    if (response.isXmlFormat()) {
      mainFormatter = newXmlFormatter();
    } else if (response.isTextFormat()) {
      mainFormatter = newTextFormatter();
    } else {
      mainFormatter = newHtmlFormatter();
    }
  }

  protected String getTitle() {
    return "Test Results";
  }

  protected String mainTemplate() {
    return "testPage";
  }

  BaseFormatter newXmlFormatter() {
    TestXmlFormatter.WriterFactory writerSource = new TestXmlFormatter.WriterFactory() {
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
        return response.getWriter();
      }
    };
    return new TestXmlFormatter(context, page, writerSource);
  }

  BaseFormatter newTextFormatter() {
    return new TestTextFormatter(response);
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
    return new TestXmlFormatter(context, page, writerFactory);
  }

  protected TestSystemListener newTestInProgressFormatter() {
    return new PageInProgressFormatter(context);
  }

  protected void performExecution() throws IOException, InterruptedException {
    List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

    MultipleTestsRunner runner = newMultipleTestsRunner(test2run);

    runner.executeTestPages();
  }

  protected MultipleTestsRunner newMultipleTestsRunner(List<WikiPage> pages) {
    final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(pages, context.root);

    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, context.runningTestingTracker, context.testSystemFactory);
    runner.setRunInProcess(debug);
    runner.setEnableRemoteDebug(remoteDebug);
    addFormatters(runner);

    return runner;
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
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

  public static class HistoryWriterFactory implements TestXmlFormatter.WriterFactory {

    public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
      File resultPath = new File(makePageHistoryFileName(context, page, counts, time));
      File resultDirectory = new File(resultPath.getParent());
      resultDirectory.mkdirs();
      File resultFile = new File(resultDirectory, resultPath.getName());
      return new PrintWriter(resultFile, "UTF-8");
    }
  }

  public static String makePageHistoryFileName(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
    return String.format("%s/%s/%s",
            context.getTestHistoryDirectory(),
            page.getPageCrawler().getFullPath().toString(),
            makeResultFileName(counts, time));
  }

  public static String makeResultFileName(TestSummary summary, long time) {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(time));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions());
  }
}
