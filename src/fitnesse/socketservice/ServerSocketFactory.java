package fitnesse.socketservice;

import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketFactory {

  /**
   * Create a new socket on the provided port number
   *
   * @param port port
   * @return A fresh socket.
   * @throws IOException Thrown if socket can not be created.
     */
  ServerSocket createServerSocket(int port) throws IOException;
  ServerSocket createLocalOnlyServerSocket(int port) throws IOException;
}
