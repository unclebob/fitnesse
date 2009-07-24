/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.*;
import java.lang.annotation.*;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.*;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.*;

import fit.Counts;

public class FitnesseSuite extends ParentRunner<TestDescriptor> {

  private Class<?> suiteClass;
  private String suiteName;
  private FitNesseRepository repository;
  private FolderTestResultRepository resultRepository;
  private fitnesse.trinidad.TestEngine testEngine;
  private List<TestDescriptor> tests;
  private SuiteResult suiteResult;

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
   * The <code>FitnesseDir</code> annotation specifies the absolute or relative
   * path to the directory in which FitNesseRoot can be found
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface FitnesseDir {
    public String value();
  }

  /**
   * The <code>Engine</code> annotation specifies which test engine will be
   * used. Currently there are two available <code>FitTestEngine.class</code>
   * and <code>SlimTestEngine.class</code>. If none is specified
   * <code>FitTestEngine.class</code> will be used.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface Engine {
    public Class<? extends TestEngine> value();
  }

  /**
   * The <code>OutputDir</code> annotation specifies where the html reports of
   * run suites and tests will be found after running them. You can either
   * specify a relative or absolute path directly, e.g.:
   * <code>@OutputDir("/tmp/trinidad-results")</code>, or you can specify a
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
   * Used by JUnit
   */
  public FitnesseSuite(Class<?> suiteClass, RunnerBuilder builder)
      throws InitializationError {
    super(suiteClass);
    this.suiteClass = suiteClass;
    try {
      suiteName = getSuiteName(suiteClass);
      repository = new FitNesseRepository(getFitnesseDir(suiteClass));
      resultRepository = new FolderTestResultRepository(
          getOutputDir(suiteClass));
      testEngine = getEngine(suiteClass);
      tests = repository.getSuite(suiteName);
    } catch (IOException e) {
      new InitializationError(e.getMessage());
    }
  }

  private TestEngine getEngine(Class<?> suiteClass) throws InitializationError {
    Engine engineClassAnnotation = suiteClass.getAnnotation(Engine.class);
    Class<? extends TestEngine> engineClass;
    if (engineClassAnnotation == null) {
      engineClass = FitTestEngine.class;
    } else {
      engineClass = engineClassAnnotation.value();
    }
    try {
      return engineClass.newInstance();
    } catch (Exception e) {
      throw new InitializationError(e.getMessage());
    }
  }

  private static String getOutputDir(Class<?> klass) throws InitializationError {
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

  private static String getFitnesseDir(Class<?> klass)
      throws InitializationError {
    FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
    if (fitnesseDirAnnotation == null) {
      throw new InitializationError("There must be a @FitnesseDir annotation");
    }
    return fitnesseDirAnnotation.value();
  }

  private static String getSuiteName(Class<?> klass) throws InitializationError {
    Name nameAnnotation = klass.getAnnotation(Name.class);
    if (nameAnnotation == null) {
      throw new InitializationError("There must be a @Name annotation");
    }
    return nameAnnotation.value();
  }

  @Override
  protected Description describeChild(TestDescriptor child) {
    return Description.createTestDescription(suiteClass, child.getName());
  }

  @Override
  protected List<TestDescriptor> getChildren() {
    return tests;
  }

  @Override
  public void run(final RunNotifier notifier) {
    try {
      repository.prepareResultRepository(resultRepository);
      suiteResult = new SuiteResult(suiteName);
      super.run(notifier);
      resultRepository.recordTestResult(suiteResult);
    } catch (IOException e) {
      notifier.fireTestFailure(new Failure(getDescription(), e));
    }
  }

  @Override
  protected void runChild(TestDescriptor test, RunNotifier notifier) {
    Description testDescription = describeChild(test);
    try {
      notifier.fireTestStarted(testDescription);
      TestResult tr = testEngine.runTest(test);
      notifyTestResult(notifier, testDescription, tr);
      suiteResult.append(tr);
      resultRepository.recordTestResult(tr);
    } catch (IOException e) {
      notifier.fireTestFailure(new Failure(testDescription, e));
    } finally {
      sleep();
    }
  }

  private void sleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }

  private void notifyTestResult(RunNotifier notifier,
      Description testDescription, TestResult tr) {
    Counts counts = tr.getCounts();
    if (counts.wrong == 0 && counts.exceptions == 0) {
      notifier.fireTestFinished(testDescription);
    } else {
      notifier.fireTestFailure(new Failure(testDescription, new AssertionError(
          "wrong: " + counts.wrong + " exceptions: " + counts.exceptions + "\n"
              + tr.getContent())));
    }
  }

  @Override
  protected String getName() {
    return suiteName;
  }

}
