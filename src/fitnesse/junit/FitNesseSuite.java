package fitnesse.junit;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.PluginException;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
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
import org.junit.runners.model.RunnerBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FitNesseSuite extends ParentRunner<WikiPage> {

  /**
   * The <code>Name</code> annotation specifies the name of the Fitnesse suite
   * to be run, e.g.: MySuite.MySubSuite
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface Name {

    public String value();
  }
  /**
   * The <code>DebugMode</code> annotation specifies whether the test is run
   * with the REST debug option. Default is true
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface DebugMode {

    public boolean value();
  }
  /**
   * The <code>SuiteFilter</code> annotation specifies the suite filter of the Fitnesse suite
   * to be run, e.g.: fasttests
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface SuiteFilter {

    public String value();
  }
  /**
   * The <code>ExcludeSuiteFilter</code> annotation specifies a filter for excluding tests from the Fitnesse suite
   * to be run, e.g.: slowtests
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ExcludeSuiteFilter {

    public String value();
  }
  /**
   * The <code>FitnesseDir</code> annotation specifies the absolute or relative
   * path to the directory in which FitNesseRoot can be found. You can either specify
   * <ul>
   * <li>a relative or absolute path directly, e.g.: <code>@FitnesseDir("/parentOfFitNesseRoot")</code>,
   * or you can specify
   * <li>a system property the content of which will be taken as base dir and
   * optionally give a path extension, e.g.:
   * <code>@FitnesseDir(systemProperty = "fitnesse.root.dir.parent")</code></li>
   * </ul>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface FitnesseDir {

    public String value() default "";
    public String systemProperty() default "";
    public String fitNesseRoot() default "FitNesseRoot";
  }
  /**
   * The <code>OutputDir</code> annotation specifies where the html reports of
   * run suites and tests will be found after running them. You can either specify
   * <ul>
   * <li>a relative or absolute path directly, e.g.: <code>@OutputDir("/tmp/trinidad-results")</code>,
   * or you can specify
   * <li>a system property the content of which will be taken as base dir and
   * optionally give a path extension, e.g.:
   * <code>@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "trinidad-results")</code></li>
   * </ul>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface OutputDir {

    public String value() default "";
    public String systemProperty() default "";

    public String pathExtension() default "";

  }
  /**
   * The <code>Port</code> annotation specifies the port used by the FitNesse
   * server. Default is the standard FitNesse port.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Deprecated
  public @interface Port {

    public int value() default 0;
    public String systemProperty() default "";

  }
  /**
   * The <code>ConfigFile</code> annotation specifies the configuration file to load.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ConfigFile {

    public String value();
  }
  private final Class<?> suiteClass;

  private final String suiteName;
  private final String outputDir;
  private final String suiteFilter;
  private final String excludeSuiteFilter;
  private final boolean debugMode;
  private final FitNesseContext context;
  private final List<WikiPage> children;

  public FitNesseSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError, IOException, PluginException {
    super(suiteClass);
    String rootPath = getFitnesseDir(suiteClass);
    String fitNesseRoot = getFitNesseRoot(suiteClass);
    int port = getPort(suiteClass);
    File configFile = getConfigFile(rootPath, suiteClass);

    this.suiteClass = suiteClass;
    this.suiteName = getSuiteName(suiteClass);
    this.outputDir = getOutputDir(suiteClass);
    this.suiteFilter = getSuiteFilter(suiteClass);
    this.excludeSuiteFilter = getExcludeSuiteFilter(suiteClass);
    this.debugMode = useDebugMode(suiteClass);
    this.context = initContext(configFile, rootPath, fitNesseRoot, port);
    this.children = initChildren();
  }

  @Override
  protected Description describeChild(WikiPage child) {
    return Description.createTestDescription(suiteClass, child.getPageCrawler().getFullPath().toString());
  }

  @Override
  protected List<WikiPage> getChildren() {
    return this.children;
  }

  static String getFitnesseDir(Class<?> klass)
          throws InitializationError {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    if (fitnesseDirAnnotation == null) {
      throw new InitializationError("There must be a @FitnesseDir annotation");
    }
    if (!"".equals(fitnesseDirAnnotation.value())) {
      return fitnesseDirAnnotation.value();
    }
    if (!"".equals(fitnesseDirAnnotation.systemProperty())) {
      String baseDir = System.getProperty(fitnesseDirAnnotation.systemProperty());
      File outputDir = new File(baseDir);
      return outputDir.getAbsolutePath();
    }
    throw new InitializationError(
            "In annotation @FitnesseDir you have to specify either 'value' or 'systemProperty'");
  }

  public static String getFitNesseRoot(Class<?> klass) {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    return fitnesseDirAnnotation.fitNesseRoot();
  }

  static String getSuiteFilter(Class<?> klass)
          throws InitializationError {
    SuiteFilter suiteFilterAnnotation = klass.getAnnotation(SuiteFilter.class);
    if (suiteFilterAnnotation == null) {
      return null;
    }
    return suiteFilterAnnotation.value();
  }

  static String getExcludeSuiteFilter(Class<?> klass)
          throws InitializationError {
    ExcludeSuiteFilter excludeSuiteFilterAnnotation = klass.getAnnotation(ExcludeSuiteFilter.class);
    if (excludeSuiteFilterAnnotation == null) {
      return null;
    }
    return excludeSuiteFilterAnnotation.value();
  }

  static String getSuiteName(Class<?> klass) throws InitializationError {
    Name nameAnnotation = klass.getAnnotation(Name.class);
    if (nameAnnotation == null) {
      throw new InitializationError("There must be a @Name annotation");
    }
    return nameAnnotation.value();
  }

  static String getOutputDir(Class<?> klass) throws InitializationError {
    OutputDir outputDirAnnotation = klass.getAnnotation(OutputDir.class);
    if (outputDirAnnotation == null) {
      throw new InitializationError("There must be a @OutputDir annotation");
    }
    if (!"".equals(outputDirAnnotation.value())) {
      return outputDirAnnotation.value();
    }
    if (!"".equals(outputDirAnnotation.systemProperty())) {
      String baseDir = System.getProperty(outputDirAnnotation.systemProperty());
      File outputDir = new File(baseDir, outputDirAnnotation.pathExtension());
      return outputDir.getAbsolutePath();
    }
    throw new InitializationError(
            "In annotation @OutputDir you have to specify either 'value' or 'systemProperty'");
  }

  public static boolean useDebugMode(Class<?> klass) {
    DebugMode debugModeAnnotation = klass.getAnnotation(DebugMode.class);
    if (null == debugModeAnnotation) {
      return true;
    }
    return debugModeAnnotation.value();
  }

  public static int getPort(Class<?> klass) {
    Port portAnnotation = klass.getAnnotation(Port.class);
    if (null == portAnnotation) {
      return 0;
    }
    int lport = portAnnotation.value();
    if (!"".equals(portAnnotation.systemProperty())) {
      lport = Integer.getInteger(portAnnotation.systemProperty(), lport);
    }
    return lport;
  }

  private File getConfigFile(String rootPath, Class<?> klass) {
    ConfigFile configFileAnnotation = klass.getAnnotation(ConfigFile.class);
    if (null == configFileAnnotation) {
      return new File(rootPath, ContextConfigurator.DEFAULT_CONFIG_FILE);
    }
    return new File(configFileAnnotation.value());
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
    try {
      executeTests(testRunner);
    } catch (AssertionError e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    } catch (Exception e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    }
  }

  private List<WikiPage> initChildren() {
    WikiPage suiteRoot = getSuiteRootPage();
    if (!suiteRoot.getData().hasAttribute("Suite")) {
      throw new IllegalArgumentException("page " + this.suiteName + " is not a suite");
    }
    return new SuiteContentsFinder(suiteRoot, new fitnesse.testrunner.SuiteFilter(suiteFilter, excludeSuiteFilter), context.root).getAllPagesToRunForThisSuite();
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
