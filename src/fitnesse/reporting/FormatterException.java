package fitnesse.reporting;

/**
 * This exception is thrown from within a formatter to denote something is
 * wrong. Throwing a runtime exception from within a formatter will
 * terminate test execution.
 */
public class FormatterException extends RuntimeException {
  public FormatterException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
