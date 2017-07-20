package fitnesse.reporting.history;

public class InvalidReportException extends Exception {
  public InvalidReportException(final String message) {
    super(message);
  }

  public InvalidReportException(final String message, final Exception exception) {
    super(message, exception);
  }
}
