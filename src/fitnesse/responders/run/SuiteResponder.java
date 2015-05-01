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
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.TraversalListener;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.InteractiveFormatter;
import fitnesse.reporting.PageInProgressFormatter;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.reporting.TestTextFormatter;
import fitnesse.reporting.history.JunitReFormatter;
import fitnesse.reporting.history.HistoryPurger;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.reporting.history.SuiteXmlReformatter;
import fitnesse.reporting.history.TestXmlFormatter;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.WikiImporter;
import fitnesse.responders.WikiImportingResponder;
import fitnesse.responders.WikiImportingTraverser;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.RunningTestingTracker;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.ConsoleExecutionLogListener;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.responders.WikiPageActions;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import org.apache.commons.lang.StringUtils;
import util.FileUtil;

import static fitnesse.responders.WikiImportingTraverser.ImportError;
import static fitnesse.wiki.WikiImportProperty.isAutoUpdated;

public class SuiteResponder extends ChunkingResponder implements SecureResponder {
  private final Logger LOG = Logger.getLogger(SuiteResponder.class.getName());

  private static final String NOT_FILTER_ARG = "excludeSuiteFilter";
  private static final String AND_FILTER_ARG = "runTestsMatchingAllTags";
  private static final String OR_FILTER_ARG_1 = "runTestsMatchingAnyTag";
  private static final String OR_FILTER_ARG_2 = "suiteFilter";

  static final RunningTestingTracker runningTestingTracker = new RunningTestingTracker();

  private final WikiImporter wikiImporter;
  private SuiteHistoryFormatter suiteHistoryFormatter;

  private PageData data;
  private String testRunId;
  private BaseFormatter mainFormatter;
  private volatile boolean isClosed = false;

  private boolean debug = false;
  private boolean remoteDebug = false;
  protected boolean includeHtml = true;
  int exitCode;

  public SuiteResponder() {
    this(new WikiImporter());
  }

  public SuiteResponder(WikiImporter wikiImporter) {
    super();
    this.wikiImporter = wikiImporter;
  }

  private boolean isInteractive() {
    return mainFormatter instanceof InteractiveFormatter;
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) {
    Response result = super.makeResponse(context, request);
    if (result != response){
        return result;
    }
    testRunId = runningTestingTracker.generateNextTicket();
    response.addHeader("X-FitNesse-Test-Id", testRunId);
    return response;
  }

  @Override
  protected void doSending() throws Exception {
    debug |= request.hasInput("debug");
    remoteDebug |= request.hasInput("remote_debug");
    includeHtml |= request.hasInput("includehtml");
    data = page.getData();

    createMainFormatter();

    if (isInteractive()) {
      makeHtml().render(response.getWriter());
    } else {
      doExecuteTests();
    }

    closeHtmlResponse(exitCode);

    cleanHistoryForSuite();
  }

  private void cleanHistoryForSuite() {
    String testHistoryDays = context.getProperty("test.history.days");
    if (withSuiteHistoryFormatter() && StringUtils.isNumeric(testHistoryDays)) {
      new HistoryPurger(context.getTestHistoryDirectory(), Integer.parseInt(testHistoryDays))
              .deleteTestHistoryOlderThanDays(path);
    }
  }

  public void doExecuteTests() {
    if (WikiImportProperty.isImported(data)) {
      importWikiPages();
    }

    try {
      performExecution();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "error registered in test system", e);
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
    htmlPage.put("headerContent", new WikiPageHeaderRenderer());
    htmlPage.put("footerContent", new WikiPageFooterRenderer());
    htmlPage.setErrorNavTemplate("errorNavigator");
    htmlPage.put("multipleTestsRun", isMultipleTestsRun());
    WikiImportingResponder.handleImportProperties(htmlPage, page);

    return htmlPage;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public class WikiPageHeaderRenderer {

    public String render() {
      return WikiPageUtil.getHeaderPageHtml(page);
    }

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

  private boolean isMultipleTestsRun() {
    return PageType.fromWikiPage(page) == PageType.SUITE;
  }

  protected void addFormatters(MultipleTestsRunner runner) {
    runner.addTestSystemListener(mainFormatter);
    if (withSuiteHistoryFormatter()) {
      addHistoryFormatter(runner);
    } else {
      runner.addExecutionLogListener(new ConsoleExecutionLogListener());
    }
    runner.addTestSystemListener(newTestInProgressFormatter());
    if (context.testSystemListener != null) {
      runner.addTestSystemListener(context.testSystemListener);
    }
  }

  private boolean withSuiteHistoryFormatter() {
    return !request.hasInput("nohistory");
  }

  protected void addHistoryFormatter(MultipleTestsRunner runner) {
    SuiteHistoryFormatter historyFormatter = getSuiteHistoryFormatter();
    runner.addTestSystemListener(historyFormatter);
    runner.addExecutionLogListener(historyFormatter);
  }

  private void createMainFormatter() {
    if (response.isXmlFormat()) {
      mainFormatter = newXmlFormatter();
    } else if (response.isTextFormat()) {
      mainFormatter = newTextFormatter();
    } else if (response.isJunitFormat()) {
      mainFormatter = newJunitFormatter();
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

  protected BaseFormatter newXmlFormatter() {
    SuiteXmlReformatter xmlFormatter = new SuiteXmlReformatter(context, page, response.getWriter(), getSuiteHistoryFormatter());
    if (includeHtml)
      xmlFormatter.includeHtml();
    if (!isMultipleTestsRun())
      xmlFormatter.includeInstructions();
    return xmlFormatter;
  }

  protected BaseFormatter newTextFormatter() {
    return new TestTextFormatter(response);
  }

  protected BaseFormatter newJunitFormatter() {
	  JunitReFormatter xmlFormatter = new JunitReFormatter(context, page, response.getWriter(), getSuiteHistoryFormatter());
	  return xmlFormatter;
  }

  protected BaseFormatter newHtmlFormatter() {
    return new SuiteHtmlFormatter(page, response.getWriter());
  }

  protected TestSystemListener<WikiTestPage> newTestInProgressFormatter() {
    return new PageInProgressFormatter(context);
  }

  protected void performExecution() throws IOException, InterruptedException {
    MultipleTestsRunner runner = newMultipleTestsRunner(getPagesToRun());
    runningTestingTracker.addStartedProcess(testRunId, runner);
    if (isInteractive()) {
      ((InteractiveFormatter) mainFormatter).setTrackingId(testRunId);
    }
    try {
      runner.executeTestPages();
    } finally {
      runningTestingTracker.removeEndedProcess(testRunId);
    }
  }

  protected List<WikiPage> getPagesToRun() {
    SuiteFilter filter = createSuiteFilter(request, page.getPageCrawler().getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    return suiteTestFinder.getAllPagesToRunForThisSuite();
  }

  protected MultipleTestsRunner newMultipleTestsRunner(List<WikiPage> pages) {
    // Add test url inputs to context's variableSource.
    final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(pages, root);

    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, context.testSystemFactory);
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
      orFilterString = request.getInput(OR_FILTER_ARG_1);
    } else {
      orFilterString = request.getInput(OR_FILTER_ARG_2);
    }
    return orFilterString;
  }

  private static String getNotSuiteFilter(Request request) {
    return request != null ? request.getInput(NOT_FILTER_ARG) : null;
  }

  private static String getAndTagFilters(Request request) {
    return request != null ? request.getInput(AND_FILTER_ARG) : null;
  }


  private static String getSuiteFirstTest(Request request, String suiteName) {
    String startTest = null;
    if (request != null) {
      startTest = request.getInput("firstTest");
    }

    if (startTest != null) {
      if (startTest.indexOf(suiteName) != 0) {
        startTest = suiteName + "." + startTest;
      }
    }

    return startTest;
  }


  public static class HistoryWriterFactory implements TestXmlFormatter.WriterFactory {

    public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
      File resultPath = new File(makePageHistoryFileName(context, page, counts, time));
      File resultDirectory = new File(resultPath.getParent());
      if (!resultDirectory.exists()) {
        resultDirectory.mkdirs();
      }
      File resultFile = new File(resultDirectory, resultPath.getName());
      return new PrintWriter(resultFile, FileUtil.CHARENCODING);
    }
  }

  public static String makePageHistoryFileName(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
    return String.format("%s/%s/%s",
            context.getTestHistoryDirectory(),
            page.getPageCrawler().getFullPath().toString(),
            makeResultFileName(counts, time));
  }

  public static String makeResultFileName(TestSummary summary, long time) {
    SimpleDateFormat format = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(time));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions());
  }

  private SuiteHistoryFormatter getSuiteHistoryFormatter() {
    if (suiteHistoryFormatter == null) {
      HistoryWriterFactory source = new HistoryWriterFactory();
      suiteHistoryFormatter = new SuiteHistoryFormatter(context, page, source);
    }
    return suiteHistoryFormatter;
  }
}
