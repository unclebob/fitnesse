package fitnesse.testsystems;

public class UnableToStopException extends Exception {
  public UnableToStopException(final String message, final Exception exception) {
    super(message, exception);
  }
}
