package fitnesse.junit;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.PluginException;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testsystems.ConsoleExecutionLogListener;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import static org.junit.Assert.*;

public abstract class FitNesseRunner extends ParentRunner<WikiPage> {
  private Class<?> suiteClass;
  private String suiteName;
  private String outputDir;
  private String suiteFilter;
  private String excludeSuiteFilter;
  private boolean debugMode;
  private FitNesseContext context;
  private List<WikiPage> children;

  public FitNesseRunner(Class<?> suiteClass) throws InitializationError {
    super(suiteClass);
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    // called by superclass' constructor
    super.collectInitializationErrors(errors);

    this.suiteClass = getTestClass().getJavaClass();

    try {
      this.suiteName = getSuiteName(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }

    try {
      this.outputDir = getOutputDir(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }

    try {
      this.suiteFilter = getSuiteFilter(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }

    try {
      this.excludeSuiteFilter = getExcludeSuiteFilter(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }

    try {
      this.debugMode = useDebugMode(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }

    try {
      this.context = createContext(suiteClass);
    } catch (Throwable t) {
      errors.add(t);
    }
  }

  protected FitNesseContext createContext(Class<?> suiteClass) throws Exception {
    String rootPath = getFitnesseDir(suiteClass);
    String fitNesseRoot = getFitNesseRoot(suiteClass);
    int port = getPort(suiteClass);
    File configFile = getConfigFile(rootPath, suiteClass);

    return initContext(configFile, rootPath, fitNesseRoot, port);
  }

  protected abstract String getSuiteName(Class<?> suiteClass) throws Exception;

  protected abstract String getOutputDir(Class<?> suiteClass) throws Exception;

  protected abstract String getSuiteFilter(Class<?> suiteClass) throws Exception;

  protected abstract String getExcludeSuiteFilter(Class<?> suiteClass) throws Exception;

  protected abstract boolean useDebugMode(Class<?> suiteClass) throws Exception;

  protected abstract String getFitnesseDir(Class<?> suiteClass) throws Exception;

  protected abstract String getFitNesseRoot(Class<?> suiteClass) throws Exception;

  protected abstract int getPort(Class<?> suiteClass) throws Exception;

  protected abstract File getConfigFile(String rootPath, Class<?> suiteClass) throws Exception;

  @Override
  protected Description describeChild(WikiPage child) {
    return Description.createTestDescription(suiteClass, child.getPageCrawler().getFullPath().toString());
  }

  @Override
  protected List<WikiPage> getChildren() {
    if (this.children == null) {
      this.children = initChildren();
    }
    return this.children;
  }

  @Override
  public void run(final RunNotifier notifier) {
    if (isFilteredForChildTest()) {
      super.run(notifier);
    } else {
      runPages(children, notifier);
    }
  }

  private boolean isFilteredForChildTest() {
    return getDescription().getChildren().size() < getChildren().size();
  }

  @Override
  protected void runChild(WikiPage page, RunNotifier notifier) {
    runPages(listOf(page), notifier);
  }

  protected void runPages(List<WikiPage>pages, final RunNotifier notifier) {
    MultipleTestsRunner testRunner = createTestRunner(pages);
    testRunner.addTestSystemListener(new JUnitRunNotifierResultsListener(notifier, suiteClass));
    testRunner.addExecutionLogListener(new ConsoleExecutionLogListener());
    try {
      executeTests(testRunner);
    } catch (AssertionError e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    } catch (Exception e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    }
  }

  protected List<WikiPage> initChildren() {
    WikiPage suiteRoot = getSuiteRootPage();
    if (suiteRoot == null) {
      throw new IllegalArgumentException("No page " + this.suiteName);
    }
    List<WikiPage> children;
    if (suiteRoot.getData().hasAttribute("Suite")) {
      children = new SuiteContentsFinder(suiteRoot, new fitnesse.testrunner.SuiteFilter(suiteFilter, excludeSuiteFilter), context.root).getAllPagesToRunForThisSuite();
    } else {
      children = Collections.singletonList(suiteRoot);
    }
    return children;
  }

  static FitNesseContext initContext(File configFile, String rootPath, String fitNesseRoot, int port) throws IOException, PluginException {
    ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults()
      .updatedWith(System.getProperties())
      .updatedWith(ConfigurationParameter.loadProperties(configFile))
      .updatedWith(ConfigurationParameter.makeProperties(
            ConfigurationParameter.PORT, port,
            ConfigurationParameter.ROOT_PATH, rootPath,
            ConfigurationParameter.ROOT_DIRECTORY, fitNesseRoot,
            ConfigurationParameter.OMITTING_UPDATES, true));

    return contextConfigurator.makeFitNesseContext();
  }

  private WikiPage getSuiteRootPage() {
    WikiPagePath path = PathParser.parse(this.suiteName);
    PageCrawler crawler = context.root.getPageCrawler();
    return crawler.getPage(path);
  }

  private MultipleTestsRunner createTestRunner(List<WikiPage> pages) {
    final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(pages, context.root);

    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, context.runningTestingTracker, context.testSystemFactory);
    runner.setRunInProcess(debugMode);
    return runner;
  }

  private void executeTests(MultipleTestsRunner testRunner) throws IOException, InterruptedException {
    JavaFormatter testFormatter = new JavaFormatter(suiteName);
    testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputDir));
    testRunner.addTestSystemListener(testFormatter);

    testRunner.executeTestPages();
    TestSummary summary = testFormatter.getTotalSummary();

    assertEquals("wrong", 0, summary.getWrong());
    assertEquals("exceptions", 0, summary.getExceptions());
    assertTrue(msgAtLeastOneTest(suiteName, summary), summary.getRight() > 0);
  }

  private String msgAtLeastOneTest(String pageName, TestSummary summary) {
    return MessageFormat.format("at least one test executed in {0}\n{1}",
            pageName, summary.toString());
  }

  private List<WikiPage> listOf(WikiPage page) {
    List<WikiPage> list = new ArrayList<WikiPage>(1);
    list.add(page);
    return list;
  }
}
