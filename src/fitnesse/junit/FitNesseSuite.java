package fitnesse.junit;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fitnesse.ContextConfigurator;
import org.junit.runners.model.InitializationError;

public class FitNesseSuite extends FitNesseRunner {

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

  public FitNesseSuite(Class<?> suiteClass) throws InitializationError {
    super(suiteClass);
  }

  @Override
  protected String getFitNesseDir(Class<?> klass)
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

  @Override
  protected String getFitNesseRoot(Class<?> klass) {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    return fitnesseDirAnnotation.fitNesseRoot();
  }

  @Override
  protected String getSuiteFilter(Class<?> klass)
          throws Exception {
    SuiteFilter suiteFilterAnnotation = klass.getAnnotation(SuiteFilter.class);
    if (suiteFilterAnnotation == null) {
      return super.getSuiteFilter(klass);
    }
    return suiteFilterAnnotation.value();
  }

  @Override
  protected String getExcludeSuiteFilter(Class<?> klass)
          throws Exception {
    ExcludeSuiteFilter excludeSuiteFilterAnnotation = klass.getAnnotation(ExcludeSuiteFilter.class);
    if (excludeSuiteFilterAnnotation == null) {
      return super.getExcludeSuiteFilter(klass);
    }
    return excludeSuiteFilterAnnotation.value();
  }

  @Override
  protected String getSuiteName(Class<?> klass) throws InitializationError {
    Name nameAnnotation = klass.getAnnotation(Name.class);
    if (nameAnnotation == null) {
      throw new InitializationError("There must be a @Name annotation");
    }
    return nameAnnotation.value();
  }

  @Override
  protected String getOutputDir(Class<?> klass) throws InitializationError {
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

  @Override
  protected boolean useDebugMode(Class<?> klass) throws Exception {
    DebugMode debugModeAnnotation = klass.getAnnotation(DebugMode.class);
    if (null == debugModeAnnotation) {
      return super.useDebugMode(klass);
    }
    return debugModeAnnotation.value();
  }

  @Override
  public int getPort(Class<?> klass) throws Exception {
    Port portAnnotation = klass.getAnnotation(Port.class);
    if (null == portAnnotation) {
      return super.getPort(klass);
    }
    int lport = portAnnotation.value();
    if (!"".equals(portAnnotation.systemProperty())) {
      lport = Integer.getInteger(portAnnotation.systemProperty(), lport);
    }
    return lport;
  }

  @Override
  protected File getConfigFile(String rootPath, Class<?> klass) throws Exception {
    ConfigFile configFileAnnotation = klass.getAnnotation(ConfigFile.class);
    if (null == configFileAnnotation) {
      return super.getConfigFile(rootPath, klass);
    }
    return new File(configFileAnnotation.value());
  }
}
