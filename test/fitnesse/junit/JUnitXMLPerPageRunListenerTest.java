package fitnesse.junit;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class JUnitXMLPerPageRunListenerTest {

  private JUnitXMLTestResultRecorder testResultRecorderMock = mock(JUnitXMLTestResultRecorder.class);

  // SUT
  private JUnitXMLPerPageRunListener jUnitXMLPerPageRunListener = new JUnitXMLPerPageRunListener(testResultRecorderMock);

  @Test
  public void testStarted() {
  }

  @Test
  public void testFinished() {
  }

  @Test
  public void testFailure() {
  }
}
