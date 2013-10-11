package fitnesse;

import fitnesse.http.Response;
import fitnesse.socketservice.SocketServerShutdownException;

/**
 * This exception can be thrown to stop the FitNesse server.
 */
public class FitNesseShutdownException extends SocketServerShutdownException {
  private final Response finalResponse;

  public FitNesseShutdownException(String reason, Response finalResponse) {
    super(reason);
    this.finalResponse = finalResponse;
  }

  public Response getFinalResponse() {
    return finalResponse;
  }
}

