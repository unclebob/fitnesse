package fitnesse.testsystems.slim;

import fitnesse.testsystems.TestExecutionException;

public class TestingInterruptedException extends TestExecutionException {
  public TestingInterruptedException(final Exception e) {
    super(e);
  }

  public TestingInterruptedException(final String message, final Exception exception) {
    super(message, exception);
  }
}
