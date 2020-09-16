package fitnesse.junit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JUnitXMLPerPageRunListenerIntegrationTest {

  @Rule
  public TemporaryFolder reportDir = new TemporaryFolder();

  private JUnitXMLReportHelper jUnitXMLReportHelper;

  // SUT
  private JUnitXMLPerPageRunListener jUnitXMLPerPageRunListener;

  @Before
  public void setUp() {
    jUnitXMLPerPageRunListener = new JUnitXMLPerPageRunListener(new JUnitXMLTestResultRecorder(reportDir.getRoot()));
    jUnitXMLReportHelper = new JUnitXMLReportHelper(reportDir.getRoot());
  }

  @Test
  public void testSuccess() {
    // given some test
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerIntegrationTest.class, "myTestName");

    // when the test is started and finished
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFinished(description);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the correct report is written to disk
    String xmlResultOnSuccess = jUnitXMLReportHelper
      .getXmlResultOnSuccess(description.getMethodName(), jUnitXMLPerPageRunListener.getExecutionTime());
    try {
      assertEquals(xmlResultOnSuccess, jUnitXMLReportHelper.readReportFile("TEST-" + description.getMethodName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void testFailure() {
    // given some test and an associated failure
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerIntegrationTest.class, "myTestName");
    Throwable throwable = new AssertionError("Gallier approved detail messsage");
    Failure failure = new Failure(description, throwable);

    // when the test is started and fails
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFailure(failure);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the correct report is written to disk
    String xmlResultOnFailure = jUnitXMLReportHelper
      .getXmlResultOnFailure(description.getMethodName(), jUnitXMLPerPageRunListener.getExecutionTime(), throwable);
    try {
      assertEquals(xmlResultOnFailure, jUnitXMLReportHelper.readReportFile("TEST-" + description.getMethodName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void testFailureWithError() {
    // given some test and an associated failure
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerIntegrationTest.class, "myTestName");
    Throwable throwable = new RuntimeException("Gallier approved detail messsage");
    Failure failure = new Failure(description, throwable);

    // when the test is started and fails
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFailure(failure);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the correct report is written to disk
    String xmlResultOnError = jUnitXMLReportHelper
      .getXmlResultOnError(description.getMethodName(), jUnitXMLPerPageRunListener.getExecutionTime(), throwable);
    try {
      assertEquals(xmlResultOnError, jUnitXMLReportHelper.readReportFile("TEST-" + description.getMethodName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }
}
