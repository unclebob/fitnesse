package fitnesse.socketservice;

import java.io.IOException;
import java.net.Socket;

public interface ClientSocketFactory {

  /**
   * Create a client socket.
   * @param hostName host to connect to.
   * @param port port to connect to.
   * @return A fresh socket.
   * @throws IOException Thrown if socket can not be opened.
     */
  Socket createSocket(String hostName, int port) throws IOException;
}
