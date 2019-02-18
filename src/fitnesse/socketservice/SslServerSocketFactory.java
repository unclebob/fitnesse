package fitnesse.socketservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SslServerSocketFactory implements ServerSocketFactory {
  private static final Logger LOG = Logger.getLogger(SslServerSocketFactory.class.getName());

  private final boolean needClientAuth;
  private final SslParameters sslParameters;

  public SslServerSocketFactory(boolean needClientAuth, SslParameters sslParameters) {
    this.needClientAuth = needClientAuth;
    this.sslParameters = sslParameters;
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    ServerSocket socket;
    LOG.log(Level.FINER, "Creating SSL socket on port: " + port);

    SSLServerSocketFactory ssf = sslParameters.createSSLServerSocketFactory();
    socket = ssf.createServerSocket(port);
    if (needClientAuth) {
      ((SSLServerSocket) socket).setNeedClientAuth(true);
    }
    return socket;
  }

  @Override
  public ServerSocket createLocalOnlyServerSocket(int port) throws IOException {
    ServerSocket socket;
    LOG.log(Level.FINER, "Creating Local-only SSL socket on port: " + port);

    SSLServerSocketFactory ssf = sslParameters.createSSLServerSocketFactory();
    socket = ssf.createServerSocket(port, 50, InetAddress.getLoopbackAddress());
    if (needClientAuth) {
      ((SSLServerSocket) socket).setNeedClientAuth(true);
    }
    return socket;
  }
}
