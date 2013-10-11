package fitnesse.socketservice;

public class SocketServerShutdownException extends Exception {

  public SocketServerShutdownException(String reason) {
    super(reason);
  }

}
