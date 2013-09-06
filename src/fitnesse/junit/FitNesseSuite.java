package fitnesse.junit;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import fitnesse.ComponentFactory;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContext.Builder;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.fs.FileSystemPageFactory;
import junit.framework.AssertionFailedError;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class FitNesseSuite extends ParentRunner<String> {

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
   * path to the directory in which FitNesseRoot can be found
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface FitnesseDir {
    public String value();
  }

  /**
   * The <code>OutputDir</code> annotation specifies where the html reports of
   * run suites and tests will be found after running them. You can either
   * specify a relative or absolute path directly, e.g.: <code>@OutputDir("/tmp/trinidad-results")</code>, or you can
   * specify a
   * system property the content of which will be taken as base dir and
   * optionally give a path extension, e.g.:
   * <code>@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "trinidad-results")</code>
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
  public @interface Port {
    public int value() default 0;

    public String systemProperty() default "";
  }

  private final Class<?> suiteClass;
  private final String suiteName;
  private String fitNesseDir;
  private String outputDir;
  private String suiteFilter;
  private String excludeSuiteFilter;
  private boolean debugMode = false;
  private int port = 0;
  private List<String> children;

  public FitNesseSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
    super(suiteClass);
    this.suiteClass = suiteClass;
    this.suiteName = getSuiteName(suiteClass);
    this.fitNesseDir = getFitnesseDir(suiteClass);
    this.outputDir = getOutputDir(suiteClass);
    this.suiteFilter = getSuiteFilter(suiteClass);
    this.excludeSuiteFilter = getExcludeSuiteFilter(suiteClass);
    this.debugMode = useDebugMode(suiteClass);
    this.port = getPort(suiteClass);

    try {
      FitNesseContext context = initContext(this.fitNesseDir, port);
      this.children = initChildren(context);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private List<String> initChildren(FitNesseContext context) {
    WikiPagePath path = PathParser.parse(this.suiteName);
    PageCrawler crawler = context.root.getPageCrawler();
    WikiPage suiteRoot = crawler.getPage(path);
    if (!suiteRoot.getData().hasAttribute("Suite")) {
      throw new IllegalArgumentException("page " + this.suiteName + " is not a suite");
    }
    WikiPage root = crawler.getPage(PathParser.parse("."));
    List<WikiPage> pages = new SuiteContentsFinder(suiteRoot, new fitnesse.testrunner.SuiteFilter(suiteFilter, excludeSuiteFilter), root).getAllPagesToRunForThisSuite();

    List<String> testPages = new ArrayList<String>();
    for (WikiPage wp : pages) {
      testPages.add(wp.getPageCrawler().getFullPath().toString());
    }
    return testPages;
  }

  @Override
  protected Description describeChild(String child) {
    return Description.createTestDescription(suiteClass, child);
  }

  @Override
  protected List<String> getChildren() {
    return this.children;
  }

  static String getFitnesseDir(Class<?> klass)
          throws InitializationError {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    if (fitnesseDirAnnotation == null) {
      throw new InitializationError("There must be a @FitnesseDir annotation");
    }
    return fitnesseDirAnnotation.value();
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

  @Override
  public void run(final RunNotifier notifier) {
    if (isFilteredForChildTest()) {
      super.run(notifier);
    } else {
      runFullSuite(notifier);
    }
  }

  protected void runFullSuite(final RunNotifier notifier) {
    JUnitHelper helper = createJUnitHelper(notifier);
    try {
      helper.assertSuitePasses(suiteName, suiteFilter, excludeSuiteFilter);
    } catch (AssertionFailedError e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    } catch (Exception e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    }
  }

  private boolean isFilteredForChildTest() {
    return getDescription().getChildren().size() < getChildren().size();
  }

  @Override
  protected void runChild(String test, RunNotifier notifier) {
    JUnitHelper helper = createJUnitHelper(notifier);
    try {
      helper.assertTestPasses(test);
    } catch (AssertionFailedError e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    } catch (Exception e) {
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
    }
  }

  private JUnitHelper createJUnitHelper(final RunNotifier notifier) {
    JUnitHelper jUnitHelper = new JUnitHelper(this.fitNesseDir, this.outputDir, new JUnitRunNotifierResultsListener(notifier, suiteClass));
    jUnitHelper.setDebugMode(debugMode);
    jUnitHelper.setPort(port);
    return jUnitHelper;
  }

  private static FitNesseContext initContext(String rootPath, int port) throws Exception {
    Builder builder = new Builder();
    WikiPageFactory wikiPageFactory = new FileSystemPageFactory();
    ComponentFactory componentFactory = new ComponentFactory(rootPath);

    builder.port = port;
    builder.rootPath = rootPath;
    builder.rootDirectoryName = "FitNesseRoot";

    builder.pageTheme = componentFactory.getProperty(ComponentFactory.THEME);
    builder.defaultNewPageContent = componentFactory
            .getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);

    builder.root = wikiPageFactory.makeRootPage(builder.rootPath,
        builder.rootDirectoryName);

    builder.logger = null;
    builder.authenticator = new PromiscuousAuthenticator();

    FitNesseContext context = builder.createFitNesseContext();
    return context;
  }
}
