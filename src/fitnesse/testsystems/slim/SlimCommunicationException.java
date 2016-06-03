package fitnesse.testsystems.slim;

import java.io.IOException;

import fitnesse.testsystems.TestExecutionException;

public class SlimCommunicationException extends TestExecutionException {
  public SlimCommunicationException(final String message, final Throwable exception) {
    super(message, exception);
  }
}
