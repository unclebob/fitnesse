package fitnesse.slim;

public class SlimIgnoreScriptTestException extends SlimException {
  private static final String IGNORE_EXCEPTION_TAG = "IGNORE_SCRIPT_TEST";

  public SlimIgnoreScriptTestException(String message) {
    super(message, IGNORE_EXCEPTION_TAG);
  }

  public SlimIgnoreScriptTestException(Throwable cause) {
    super(cause, IGNORE_EXCEPTION_TAG);
  }

  public SlimIgnoreScriptTestException(String message, Throwable cause) {
    super(message, cause, IGNORE_EXCEPTION_TAG);
  }
}
