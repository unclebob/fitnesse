package fitnesse.socketservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sun.security.x509.X500Name;
import util.StreamReader;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SocketFactory {
  private static final Logger LOG = Logger.getLogger(SocketFactory.class.getName());
  private SocketFactory() {
    
  }

  public static ServerSocket tryCreateServerSocket(int port) throws IOException {
	return tryCreateServerSocket(port, false, false, null);
  }

  
@SuppressWarnings("resource")
	public static ServerSocket tryCreateServerSocket(int port, boolean useSSL, boolean needClientAuth, String sslParameterClassName) throws IOException {
	  ServerSocket socket;
		  if(!useSSL){ 	
			LOG.log(Level.FINER, "Creating none SSL socket on port: " + port);
		    socket = new ServerSocket(port);
		  }else{
		    LOG.log(Level.FINER, "Creating SSL socket on port: " + port);
		    
		    SSLServerSocketFactory ssf = SslParameters.setSslParameters(sslParameterClassName).createSSLServerSocketFactory(); 
			socket = ssf.createServerSocket(port);
		    if (needClientAuth){
		      ((SSLServerSocket)socket).setNeedClientAuth(true);
		    }
		  }

	  return socket;
	}

	public static Socket tryCreateClientSocket(String hostName, int port, boolean useSSL, String sslParameterClassName) throws IOException {
	    if(!useSSL){ 	
		  LOG.log(Level.FINER, "Creating none SSL client: " + hostName + ":" + port);
	  	  return new Socket(hostName, port);
	    }else{
	      LOG.log(Level.FINER, "Creating SSL client: " + hostName + ":" + port );
	      SSLSocketFactory ssf = SslParameters.setSslParameters(sslParameterClassName).createSSLSocketFactory();
	      SSLSocket socket = (SSLSocket) ssf.createSocket(hostName,port);
	      LOG.log(Level.FINER, "Starting SSL Handshake.");
	      //socket.setSoTimeout(1000);
		  socket.startHandshake();
		  printSocketInfo(socket);
		    
	      return socket;
	    }
	}


  public static boolean isSSLSocket(Socket theSocket) {
	return (theSocket instanceof SSLSocket);
  }

	
  public static BufferedOutputStream getByteWriter(Socket socket) throws IOException {
	  return new BufferedOutputStream(socket.getOutputStream());
  }

  public static StreamReader getReader(Socket socket) throws IOException {
	  return new StreamReader(new BufferedInputStream(socket.getInputStream()));
  }

  public static String peerName(Socket theSocket){
	  if(isSSLSocket(theSocket)){
		  SSLSession ss = ((SSLSocket)theSocket).getSession();
	      try {
			return new X500Name(ss.getPeerPrincipal().getName()).getCommonName();
		} catch (SSLPeerUnverifiedException e) {
			LOG.log(Level.FINEST, "Could not get Peer Name: not verified: " + e.getMessage());
			return null;
		} catch (IOException e) {
			LOG.log(Level.FINEST, "Could not get Peer Name: " + e.getMessage());
			return null;
		}
	  }
	  else{
		  return null;
	  }
  }

  public static String myName(Socket theSocket){
	  if(isSSLSocket(theSocket)){
		  SSLSession ss = ((SSLSocket)theSocket).getSession();
	      try {
			return new X500Name(ss.getLocalPrincipal().getName()).getCommonName();
		} catch (IOException e) {
			LOG.log(Level.FINEST, "Could not get my name: " + e.getMessage());
			return null;
		}
	  }
	  else{
		  return null;
	  }
  }

  public static void printSocketInfo(Socket theSocket) {
	
	
	LOG.log(Level.FINER, "Socket class: "+theSocket.getClass());
    LOG.log(Level.FINER, "   Remote address = " 	    +theSocket.getRemoteSocketAddress().toString());
    LOG.log(Level.FINER, "   Local socket address = " +theSocket.getLocalSocketAddress().toString());
    LOG.log(Level.FINEST, "   Closed = " 		         +theSocket.isClosed());
    LOG.log(Level.FINEST, "   Connected = " 	 	     +theSocket.isConnected());
    LOG.log(Level.FINEST, "   Bound = " 	 	         +theSocket.isBound());
    LOG.log(Level.FINEST, "   isInputShutdown = " 	 	 +theSocket.isInputShutdown());
    LOG.log(Level.FINEST, "   isOutputShutdown = " 	 +theSocket.isOutputShutdown());
	if (isSSLSocket(theSocket)){
		SSLSocket s = (SSLSocket) theSocket;
	    LOG.log(Level.FINEST, "   Need client authentication = "  + s.getNeedClientAuth());
	    LOG.log(Level.FINEST, "   Want client authentication = "  + s.getWantClientAuth());
	    LOG.log(Level.FINEST, "   Use client mode = " 		       + s.getUseClientMode());
	    
	    try {
	      SSLSession ss = s.getSession();
	      LOG.log(Level.FINEST, "Session class: " +ss.getClass());
 	      LOG.log(Level.FINEST, "   ID is " + new BigInteger(ss.getId()));
	      LOG.log(Level.FINEST, "   Session created in " + ss.getCreationTime());
	      LOG.log(Level.FINEST, "   Session accessed in " + ss.getLastAccessedTime());
	      LOG.log(Level.FINEST, "   Cipher suite = " +ss.getCipherSuite());
	      LOG.log(Level.FINEST, "   Protocol = "+ss.getProtocol());
	      LOG.log(Level.FINEST, "   LocalPrincipal = " +ss.getLocalPrincipal().getName());
	      LOG.log(Level.FINEST, "   PeerPrincipal = " +ss.getPeerPrincipal().getName());
	      LOG.log(Level.FINE, "   PeerName = " + peerName(s));
	      
		  Certificate[] cchain = ss.getPeerCertificates();
		  LOG.log(Level.FINEST, "The Certificates used by peer");
		  for (int i = 0; i < cchain.length; i++) {
			LOG.log(Level.FINEST, "   " +cchain[i].toString());
		    LOG.log(Level.FINEST, "   " + ((X509Certificate) cchain[i]).getSubjectDN());
		  }
		
	    }catch (Exception e) {
	      LOG.warning(e.toString());
	    }
	}    
  }
}