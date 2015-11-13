// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import static fitnesse.socketservice.SocketServer.StreamUtility.GetBufferedReader;
import static fitnesse.socketservice.SocketServer.StreamUtility.GetPrintStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SslSocketServiceTest {
  private int connections = 0;
  private SocketServer connectionCounter;
  private SocketService ss;
  private static final int PORT_NUMBER = 1999;

  public SslSocketServiceTest() {
    connectionCounter = new SocketServer() {
      @Override
      public void serve(Socket s) {
        connections++;
      }
    };
  }

  @Before
  public void setUp() throws Exception {
    connections = 0;
  }

  @Test
  public void testNoConnections() throws Exception {
    SocketServer connectionCounter1 = this.connectionCounter;
    ss = createSslSocketService(connectionCounter1);
    ss.close();
    assertEquals(0, connections);
  }

  public SocketService createSslSocketService(SocketServer socketServer) throws IOException {
    return new SocketService(socketServer, true, SocketFactory.createSslServerSocket(PORT_NUMBER, false, "fitnesse.socketservice.SslParametersWiki"));
  }

  private Socket createClientSocket(int port) throws IOException {
    return SocketFactory.createClientSocket("localhost", port, true, "fitnesse.socketservice.SslParametersWiki");
  }

  @Test
  public void testOneConnection() throws Exception {
	  ss = createSslSocketService(connectionCounter);
    connect(PORT_NUMBER);
    ss.close();
    assertEquals(1, connections);
  }

  @Test
  public void testManyConnections() throws Exception {
    ss = createSslSocketService(new EchoService());
    String answer = "";
    for (int i = 0; i < 10; i++){
        Socket s = createClientSocket(PORT_NUMBER);
    	  System.out.print("Peer: " + SocketFactory.peerName(s) + "\n");
        BufferedReader br = GetBufferedReader(s);
        PrintStream ps = GetPrintStream(s);
        ps.println(i + ",");
        answer = answer + br.readLine();
    }
    ss.close();

   System.out.print("Got Messages : " +answer +"\n");
   assertEquals("0,1,2,3,4,5,6,7,8,9,", answer);
  }

  @Test
  public void testSendMessage() throws Exception {
	  ss = createSslSocketService(new HelloService());

    Socket s = createClientSocket(PORT_NUMBER);

    BufferedReader br = GetBufferedReader(s);
    String answer = br.readLine();
    s.close();
    ss.close();
    assertEquals("Hello", answer);
  }

  @Test
  public void testReceiveMessage() throws Exception {
	  ss = createSslSocketService(new EchoService());
    Socket s = createClientSocket(PORT_NUMBER);
    BufferedReader br = GetBufferedReader(s);
    PrintStream ps = GetPrintStream(s);
    ps.println("MyMessage");
    String answer = br.readLine();
    s.close();
    ss.close();
    assertEquals("MyMessage", answer);
  }

  private void connect(int port) {
    try {
      Socket s = createClientSocket(port);
      sleep(30);
      s.close();
    }
    catch (IOException e) {
      fail("could not connect");
    }
  }

  private void sleep(int ms) {
    try {
      Thread.sleep(ms);
    }
    catch (InterruptedException e) {
    }
  }
}

class HelloService2 implements SocketServer {
  @Override
  public void serve(Socket s) {
    try {
      PrintStream ps = GetPrintStream(s);
      ps.println("Hello");
    }
    catch (IOException e) {
    }
  }
}

class EchoService2 implements SocketServer {
  @Override
  public void serve(Socket s) {
    try {
      PrintStream ps = GetPrintStream(s);
      BufferedReader br = GetBufferedReader(s);
      String token = br.readLine();
      ps.println(token);
    }
    catch (IOException e) {
    }
  }
}
