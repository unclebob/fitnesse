// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import static fitnesse.socketservice.SocketServer.StreamUtility.GetBufferedReader;
import static fitnesse.socketservice.SocketServer.StreamUtility.GetPrintStream;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SslSocketServiceTest {
  private int connections = 0;
  private SocketServer connectionCounter;
  private SocketService ss;
  private static final int RANDOM_PORT = 0;

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
    ServerSocket serverSocket = createServerSocket();
    ss = createSslSocketService(connectionCounter1, serverSocket);
    ss.close();
    assertEquals(0, connections);
  }

  public SocketService createSslSocketService(SocketServer socketServer, ServerSocket serverSocket) throws IOException {
    return new SocketService(socketServer, true, serverSocket);
  }

  private ServerSocket createServerSocket() throws IOException {
    return new SslServerSocketFactory(false, "fitnesse.socketservice.SslParametersWiki").createServerSocket(RANDOM_PORT);
  }

  private Socket createClientSocket(int port) throws IOException {
    return new SslClientSocketFactory("fitnesse.socketservice.SslParametersWiki").createSocket("localhost", port);
  }

  @Test
  public void testManyConnections() throws Exception {
    ServerSocket serverSocket = createServerSocket();
    ss = createSslSocketService(new EchoService(), serverSocket);
    List<String> answers = new ArrayList<>();
    for (int i = 0; i < 10; i++){
      Socket s = createClientSocket(serverSocket.getLocalPort());
      BufferedReader br = GetBufferedReader(s);
      PrintStream ps = GetPrintStream(s);
      ps.println(i);
      String answer = br.readLine();
      assertEquals(String.valueOf(i), answer);
      answers.add(answer);
    }
    ss.close();
    assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", answers.toString());
  }

  @Test
  public void testSendMessage() throws Exception {
    ServerSocket serverSocket = createServerSocket();
    ss = createSslSocketService(new HelloService(), serverSocket);

    Socket s = createClientSocket(serverSocket.getLocalPort());

    BufferedReader br = GetBufferedReader(s);
    String answer = br.readLine();
    s.close();
    ss.close();
    assertEquals("Hello", answer);
  }

  @Test
  public void testReceiveMessage() throws Exception {
    ServerSocket serverSocket = createServerSocket();
	  ss = createSslSocketService(new EchoService(), serverSocket);
    Socket s = createClientSocket(serverSocket.getLocalPort());
    BufferedReader br = GetBufferedReader(s);
    PrintStream ps = GetPrintStream(s);
    ps.println("MyMessage");
    String answer = br.readLine();
    s.close();
    ss.close();
    assertEquals("MyMessage", answer);
  }

}
