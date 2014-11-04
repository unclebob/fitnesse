package fitnesse.socketservice;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

public final class SocketFactory {
  private SocketFactory() {
    
  }
  
  public static ServerSocket tryCreateServerSocket(int port) throws IOException {
    ServerSocket socket;
    try {
      socket = new ServerSocket(port);
    } catch (BindException e) {
      throw new IOException("Bind exception on port " + port + ": " + e.getMessage(), e);
    } catch (IOException e) {
      throw new IOException("IO exception on port " + port + ": " + e.getMessage(), e);
    }

    return socket;
  }
}
