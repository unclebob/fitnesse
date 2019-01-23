package fitnesse.socketservice;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SslClientSocketFactory implements ClientSocketFactory {
  private static final Logger LOG = Logger.getLogger(SslClientSocketFactory.class.getName());
  private static final String COMMONNAME = "CN";

  private final SslParameters sslParameters;

  public SslClientSocketFactory(final SslParameters sslParameters) {
    this.sslParameters = sslParameters;
  }

  @Override
  public Socket createSocket(final String hostName, final int port) throws IOException {
    LOG.log(Level.FINER, "Creating SSL client: " + hostName + ":" + port);
    SSLSocketFactory ssf = sslParameters.createSSLSocketFactory();
    SSLSocket socket = (SSLSocket) ssf.createSocket(hostName, port);
    LOG.log(Level.FINER, "Starting SSL Handshake.");
    //socket.setSoTimeout(1000);
    socket.startHandshake();
    printSocketInfo(socket);

    return socket;
  }


  public static boolean isSSLSocket(Socket theSocket) {
    return (theSocket instanceof SSLSocket);
  }

  public static String peerName(Socket theSocket) {
    String peerDn = peerDn(theSocket);
    if (peerDn == null) {
      return null;
    } else {
      return getRdnByNameFromDn(peerDn, COMMONNAME);
    }
  }

  public static String myName(Socket theSocket) {
    if (isSSLSocket(theSocket)) {
      SSLSession ss = ((SSLSocket) theSocket).getSession();

      String dn = ss.getLocalPrincipal().getName();
      return getRdnByNameFromDn(dn, COMMONNAME);
    } else {
      return null;
    }
  }

  public static String peerDn(Socket theSocket) {
    if (isSSLSocket(theSocket)) {
      SSLSession ss = ((SSLSocket) theSocket).getSession();
      try {
        return ss.getPeerPrincipal().getName();
      } catch (SSLPeerUnverifiedException e) {
        LOG.log(Level.FINEST, "Could not get Peer Name: not verified: " + e.getMessage());
        return null;
      }
    } else {
      return null;
    }
  }


  public static String getRdnByNameFromDn(String dn, String rdnType) {
    if (dn == null) {
      return null;
    } else {
      try {
        LdapName ldapDN = new LdapName(dn);
        for (Rdn rdn : ldapDN.getRdns()) {
          if (rdn.getType().equalsIgnoreCase(rdnType)) return rdn.getValue().toString();
        }
        LOG.log(Level.FINEST, "Could not find RDN Type '" + rdnType + "' in DN '" + dn + "'");
        return null;
      } catch (InvalidNameException e) {
        LOG.log(Level.FINEST, "Invalid DN '" + dn + "' :" + e.getMessage());
        return null;
      }
    }
  }

  public static void printSocketInfo(Socket theSocket) {

    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "Socket class: " + theSocket.getClass());
      LOG.log(Level.FINER, "   Remote address = " + theSocket.getRemoteSocketAddress().toString());
      LOG.log(Level.FINER, "   Local socket address = " + theSocket.getLocalSocketAddress().toString());
    }

    if (LOG.isLoggable(Level.FINEST)) {
      LOG.log(Level.FINEST, "   Closed = " + theSocket.isClosed());
      LOG.log(Level.FINEST, "   Connected = " + theSocket.isConnected());
      LOG.log(Level.FINEST, "   Bound = " + theSocket.isBound());
      LOG.log(Level.FINEST, "   isInputShutdown = " + theSocket.isInputShutdown());
      LOG.log(Level.FINEST, "   isOutputShutdown = " + theSocket.isOutputShutdown());
    }

    if (isSSLSocket(theSocket)) {
      SSLSocket s = (SSLSocket) theSocket;
      SSLSession ss = s.getSession();

      if (LOG.isLoggable(Level.FINEST)) {
        LOG.log(Level.FINEST, "   Need client authentication = " + s.getNeedClientAuth());

        LOG.log(Level.FINEST, "   Want client authentication = " + s.getWantClientAuth());
        LOG.log(Level.FINEST, "   Use client mode = " + s.getUseClientMode());

        LOG.log(Level.FINEST, "Session class: " + ss.getClass());
        LOG.log(Level.FINEST, "   ID is " + new BigInteger(ss.getId()));
        LOG.log(Level.FINEST, "   Session created in " + ss.getCreationTime());
        LOG.log(Level.FINEST, "   Session accessed in " + ss.getLastAccessedTime());
        LOG.log(Level.FINEST, "   Cipher suite = " + ss.getCipherSuite());
        LOG.log(Level.FINEST, "   Protocol = " + ss.getProtocol());
        LOG.log(Level.FINEST, "   LocalPrincipal = " + ss.getLocalPrincipal().getName());
        try {
          LOG.log(Level.FINEST, "   PeerPrincipal = " + ss.getPeerPrincipal().getName());
        } catch (SSLPeerUnverifiedException e) {
          LOG.warning("Could not retrieve Peer principal information: " + e.getMessage());
        }
      }

      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "   PeerName = " + peerName(s));
      }

      if (LOG.isLoggable(Level.FINEST)) {
        Certificate[] cchain = new Certificate[0];
        try {
          cchain = ss.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
          LOG.warning("Could not retrieve certeficate for peer: " + e.getMessage());
        }
        LOG.log(Level.FINEST, "The Certificates used by peer");
        for (Certificate aCchain : cchain) {
          LOG.log(Level.FINEST, "   " + aCchain.toString());
          LOG.log(Level.FINEST, "   " + ((X509Certificate) aCchain).getSubjectDN());
        }
      }
    }
  }

}
