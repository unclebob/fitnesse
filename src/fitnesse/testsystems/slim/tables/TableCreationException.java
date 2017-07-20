package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.TestExecutionException;

public class TableCreationException extends TestExecutionException {
  public TableCreationException(final Throwable cause) {
    super(cause);
  }

  public TableCreationException(final String message) {
    super(message);
  }

  public TableCreationException(final String message, final Throwable e) {
    super(message, e);
  }
}
