package fitnesse.slim;

public class StopTestSlimException extends SlimException {
  private static final String STOP_EXCEPTION_TAG = "ABORT_SLIM_TEST";

  public StopTestSlimException(String message) {
    super(message, STOP_EXCEPTION_TAG);
  }

  public StopTestSlimException(Throwable cause) {
    super(cause, STOP_EXCEPTION_TAG);
  }

  public StopTestSlimException(String message, Throwable cause) {
    super(message, cause, STOP_EXCEPTION_TAG);
  }
}
