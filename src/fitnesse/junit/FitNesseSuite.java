package fitnesse.junit;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import fitnesse.responders.run.JavaFormatter;

public class FitNesseSuite extends ParentRunner<String>{

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
      
  private final Class<?> suiteClass;
  private final String suiteName;
  private String fitNesseDir;
  private String outputDir;
  public FitNesseSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
    super(suiteClass);
    this.suiteClass = suiteClass;
    this.suiteName=getSuiteName(suiteClass);
    this.fitNesseDir=getFitnesseDir(suiteClass);
    this.outputDir=getOutputDir(suiteClass);
  }
  @Override
  protected Description describeChild(String child) {
    return Description.createTestDescription(suiteClass, child);
  }
 
  @Override
  protected List<String> getChildren() {
    return JavaFormatter.getInstance(suiteName).getTestsExecuted();
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
  @Override
  public void run(final RunNotifier notifier) { 
      JUnitHelper helper=new JUnitHelper(this.fitNesseDir, this.outputDir, new JUnitRunNotifierResultsListener(notifier,suiteClass));
      try{
        helper.assertSuitePasses(suiteName);
      }catch(Exception e){
        notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass),e));
      }
  }
 
  @Override
  protected void runChild(String test, RunNotifier notifier) {
    JUnitHelper helper=new JUnitHelper(this.fitNesseDir, this.outputDir, new JUnitRunNotifierResultsListener(notifier,suiteClass));
    try{
      helper.assertTestPasses(suiteName);
    }catch(Exception e){
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass),e));
    }
  }
}
