package fitnesse.junit;

import fitnesse.util.TimeMeasurement;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;

/**
 * JUnit RunListener to be used during integration test executing FitNesse pages.
 * It will create a result XML file per FitNesse page, instead of default (surefire/maven) behavior that creates only
 * 1 file per Java class (and we have only 1 class that runs all pages).
 * This allows build servers to report progress during the run.
 * The page names are used as test names, the Java class executing them is ignored.
 * <p><br>
 * Usage example:
 * <pre>
 * {@code
 * <plugin>
 *   <artifactId>maven-failsafe-plugin</artifactId>
 *   ...
 *   <configuration>
 *   ...
 *     <properties>
 *       <property>
 *         <name>listener</name>
 *         <value>fitnesse.junit.JUnitXMLPerPageRunListener</value>
 *       </property>
 *     </properties>
 *     <disableXmlReport>true</disableXmlReport>
 *   </configuration>
 * </plugin>
 * }
 * </pre>
 * NOTE: The standard JUnit xml reports have to be disabled, as shown in example above.
 */
public class JUnitXMLPerPageRunListener extends RunListener {
  // default directory for maven-failsafe-plugin
  private static final String OUTPUT_PATH = "target/failsafe-reports/";
  private final JUnitXMLTestResultRecorder testResultRecorder;
  private TimeMeasurement timeMeasurement;

  /**
   * Creates new.
   */
  public JUnitXMLPerPageRunListener() {
    this.testResultRecorder = new JUnitXMLTestResultRecorder(new File(OUTPUT_PATH));
  }

  /**
   * Creates new.
   * @param jUnitXMLTestResultRecorder the recorder used to record the test results
   */
  public JUnitXMLPerPageRunListener(JUnitXMLTestResultRecorder jUnitXMLTestResultRecorder) {
    this.testResultRecorder = jUnitXMLTestResultRecorder;
  }

  @Override
  public void testStarted(Description description) throws Exception {
    timeMeasurement = new TimeMeasurement().start();
    super.testStarted(description);
  }

  @Override
  public void testFinished(Description description) throws Exception {
    super.testFinished(description);
    if (!timeMeasurement.isStopped()) {
      testResultRecorder.recordTestResult(getTestName(description), 0, 0, 0, null, getExecutionTime());
    }
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    super.testFailure(failure);
    String testName = getTestName(failure.getDescription());
    Throwable throwable = failure.getException();
    long executionTime = getExecutionTime();
    if (throwable instanceof AssertionError) {
      testResultRecorder.recordTestResult(testName, 0, 1, 0, throwable, executionTime);
    } else {
      testResultRecorder.recordTestResult(testName, 0, 0, 1, throwable, executionTime);
    }
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    super.testIgnored(description);
    if (!timeMeasurement.isStopped()) {
      testResultRecorder.recordTestResult(getTestName(description), 1, 0, 0, null, getExecutionTime());
    }
  }

  /**
   * @return test execution time in milliseconds
   */
  protected long getExecutionTime() {
    long executionTime = 0;
    if (timeMeasurement != null) {
      executionTime = timeMeasurement.elapsed();
      if (!timeMeasurement.isStopped()) {
        timeMeasurement.stop();
      }
    }
    return executionTime;
  }

  /**
   * @return directory to store test XMLs in
   */
  protected String getOutputPath() {
    return OUTPUT_PATH;
  }

  /**
   * @param description JUnit description of test executed
   * @return name to use in report
   */
  protected String getTestName(Description description) {
    return description.getMethodName();
  }
}
