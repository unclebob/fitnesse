package fitnesse.socketservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlainServerSocketFactory implements ServerSocketFactory {
  private static final Logger LOG = Logger.getLogger(PlainServerSocketFactory.class.getName());

  @Override
  public ServerSocket createServerSocket(final int port) throws IOException {
    LOG.log(Level.FINER, "Creating plain socket on port: " + port);
    return new ServerSocket(port);
  }

  @Override
  public ServerSocket createLocalOnlyServerSocket(int port) throws IOException {
    LOG.log(Level.FINER, "Creating loopback only plain socket on port: " + port);
    return new ServerSocket(port, 50, InetAddress.getLoopbackAddress());
  }
}
