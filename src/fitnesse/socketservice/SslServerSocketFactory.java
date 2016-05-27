package fitnesse.socketservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SslServerSocketFactory implements ServerSocketFactory {
  private static final Logger LOG = Logger.getLogger(SslServerSocketFactory.class.getName());

  private final boolean needClientAuth;
  private final String sslParameterClassName;

  public SslServerSocketFactory(boolean needClientAuth, String sslParameterClassName) {
    this.needClientAuth = needClientAuth;
    this.sslParameterClassName = sslParameterClassName;
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    ServerSocket socket;
    LOG.log(Level.FINER, "Creating SSL socket on port: " + port);

    SSLServerSocketFactory ssf = SslParameters.setSslParameters(sslParameterClassName).createSSLServerSocketFactory();
    socket = ssf.createServerSocket(port);
    if (needClientAuth) {
      ((SSLServerSocket) socket).setNeedClientAuth(true);
    }
    return socket;
  }
}
