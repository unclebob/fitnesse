package fitnesse.testsystems;

public class TestExecutionException extends Exception {
  public TestExecutionException(final Throwable cause) {
    super(cause);
  }

  public TestExecutionException(final String message) {
    super(message);
  }

  public TestExecutionException(final String message, final Throwable e) {
    super(message, e);
  }
}
