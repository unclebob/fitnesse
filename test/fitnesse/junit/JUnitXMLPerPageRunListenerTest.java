package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class JUnitXMLPerPageRunListenerTest {

  private JUnitXMLTestResultRecorder testResultRecorderMock = mock(JUnitXMLTestResultRecorder.class);

  // SUT
  private JUnitXMLPerPageRunListener jUnitXMLPerPageRunListener = new JUnitXMLPerPageRunListener(testResultRecorderMock);

  @Test
  public void testStarted() {
    // given some test
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerTest.class, "myTestName");

    // when the test is started
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void testSuccess() {
    // given some test
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerTest.class, "myTestName");

    // when the test is started and finished
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFinished(description);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(description.getMethodName(), 0, 0, 0, null, jUnitXMLPerPageRunListener.getExecutionTime());
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void testFailure() {
    // given some test and an associated failure
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerTest.class, "myTestName");
    Throwable throwable = new AssertionError("Gallier approved detail messsage");
    Failure failure = new Failure(description, throwable);

    // when the test is started and fails
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFailure(failure);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(description.getMethodName(), 0, 1, 0, throwable, jUnitXMLPerPageRunListener.getExecutionTime());
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void testFailureWithError() {
    // given some test and an associated failure
    Description description = Description.createTestDescription(JUnitXMLPerPageRunListenerTest.class, "myTestName");
    Throwable throwable = new RuntimeException("Gallier approved detail messsage");
    Failure failure = new Failure(description, throwable);

    // when the test is started and fails
    try {
      jUnitXMLPerPageRunListener.testStarted(description);
      jUnitXMLPerPageRunListener.testFailure(failure);
    } catch (Exception e) {
      fail("Exception was caught but should have never been thrown.");
    }

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(description.getMethodName(), 0, 0, 1, throwable, jUnitXMLPerPageRunListener.getExecutionTime());
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }
}
