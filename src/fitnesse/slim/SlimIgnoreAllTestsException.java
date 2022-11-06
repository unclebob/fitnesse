package fitnesse.slim;

public class SlimIgnoreAllTestsException extends SlimException {
  private static final String IGNORE_EXCEPTION_TAG = "IGNORE_ALL_TESTS";

  public SlimIgnoreAllTestsException(String message) {
    super(message, IGNORE_EXCEPTION_TAG);
  }

  public SlimIgnoreAllTestsException(Throwable cause) {
    super(cause, IGNORE_EXCEPTION_TAG);
  }

  public SlimIgnoreAllTestsException(String message, Throwable cause) {
    super(message, cause, IGNORE_EXCEPTION_TAG);
  }
}
