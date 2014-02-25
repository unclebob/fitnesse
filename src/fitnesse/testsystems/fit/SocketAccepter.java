package fitnesse.testsystems.fit;

import java.io.IOException;
import java.net.Socket;

public interface SocketAccepter {
  void acceptSocket(Socket socket) throws IOException, InterruptedException;
}
