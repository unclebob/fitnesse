package fitnesse.socketservice;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlainClientSocketFactory implements ClientSocketFactory {
  private static final Logger LOG = Logger.getLogger(PlainClientSocketFactory.class.getName());

  @Override
  public Socket createSocket(final String hostName, final int port) throws IOException {
    LOG.log(Level.FINER, "Creating plain client: " + hostName + ":" + port);
    return new Socket(hostName, port);

  }
}
