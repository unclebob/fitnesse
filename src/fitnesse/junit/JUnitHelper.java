package fitnesse.junit;


import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testsystems.ConsoleExecutionLogListener;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JUnitHelper {

  public static final String PAGE_TYPE_SUITE="suite";
  public static final String PAGE_TYPE_TEST="test";
  private final String outputDir;
  private final String fitNesseRootPath;
  private final TestSystemListener resultsListener;
  private int port = 0;
  private boolean debugMode = true;

  public JUnitHelper(String fitNesseRootPath, String outputPath) {
    this(fitNesseRootPath, outputPath, new PrintTestListener());
  }

  public JUnitHelper(String fitNesseDir, String outputDir,
                     TestSystemListener resultsListener) {
    this.fitNesseRootPath = fitNesseDir;
    this.outputDir = outputDir;
    this.resultsListener = resultsListener;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setDebugMode(boolean enabled) {
    debugMode = enabled;
  }

  public void assertTestPasses(String testName) throws Exception {
    assertPasses(testName, PAGE_TYPE_TEST, null);
  }

  public void assertSuitePasses(String suiteName) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, null);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, suiteFilter);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter, String excludeSuiteFilter) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, suiteFilter, excludeSuiteFilter);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception {
    assertPasses(pageName, pageType, suiteFilter, null);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter, String excludeSuiteFilter) throws Exception {
    FitNesseContext context = FitNesseRunner.initContext(new File(ContextConfigurator.DEFAULT_CONFIG_FILE), fitNesseRootPath, ContextConfigurator.DEFAULT_ROOT, port);

    JavaFormatter testFormatter = new JavaFormatter(pageName);
    testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputDir));

    MultipleTestsRunner testRunner = createTestRunner(initChildren(pageName, suiteFilter, excludeSuiteFilter, context), context);
    testRunner.addTestSystemListener(testFormatter);
    testRunner.addTestSystemListener(resultsListener);
    testRunner.addExecutionLogListener(new ConsoleExecutionLogListener());

    testRunner.executeTestPages();
    TestSummary summary = testFormatter.getTotalSummary();

    assertEquals("wrong", 0, summary.getWrong());
    assertEquals("exceptions", 0, summary.getExceptions());
    assertTrue(msgAtLeastOneTest(pageName, summary), summary.getRight() > 0);
  }

  private List<WikiPage> initChildren(String suiteName, String suiteFilter, String excludeSuiteFilter, FitNesseContext context) {
    WikiPage suiteRoot = getSuiteRootPage(suiteName, context);
    if (!suiteRoot.getData().hasAttribute("Suite")) {
      return Arrays.asList(suiteRoot);
    }
    return new SuiteContentsFinder(suiteRoot, new fitnesse.testrunner.SuiteFilter(suiteFilter, excludeSuiteFilter), context.getRootPage()).getAllPagesToRunForThisSuite();
  }

  private WikiPage getSuiteRootPage(String suiteName, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(suiteName);
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    return crawler.getPage(path);
  }

  private MultipleTestsRunner createTestRunner(List<WikiPage> pages, FitNesseContext context) {
    final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(pages, context.getRootPage());

    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, context.testSystemFactory);
    runner.setRunInProcess(debugMode);
    return runner;
  }

  private String msgAtLeastOneTest(String pageName, TestSummary summary) {
    return
      MessageFormat.format("at least one test executed in {0}\n{1}",
        pageName, summary.toString());
  }
}
