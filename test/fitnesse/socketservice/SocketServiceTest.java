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
import org.junit.Test;

public class SocketServiceTest {
  private int connections = 0;
  private SocketServer connectionCounter;
  private SocketService ss;
  private static final int PORT_NUMBER = 1999;

  public SocketServiceTest() {
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
    ss = createSocketService(connectionCounter1);
    ss.close();
    assertEquals(0, connections);
  }

  public SocketService createSocketService(SocketServer socketServer) throws IOException {
    return new SocketService(socketServer, false, new PlainServerSocketFactory().createServerSocket(PORT_NUMBER));
  }

  @Test
  public void testOneConnection() throws Exception {
    ss = createSocketService(connectionCounter);
    connect(PORT_NUMBER);
    ss.close();
    assertEquals(1, connections);
  }

  @Test
  public void testManyConnections() throws Exception {
    ss = createSocketService(connectionCounter);
    for (int i = 0; i < 10; i++)
      connect(PORT_NUMBER);
    ss.close();
    assertEquals(10, connections);
  }

  @Test
  public void testSendMessage() throws Exception {
    ss = createSocketService(new HelloService());
    Socket s = new Socket("localhost", PORT_NUMBER);
    BufferedReader br = GetBufferedReader(s);
    String answer = br.readLine();
    s.close();
    ss.close();
    assertEquals("Hello", answer);
  }

  @Test
  public void testReceiveMessage() throws Exception {
    ss = createSocketService(new EchoService());
    Socket s = new Socket("localhost", PORT_NUMBER);
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
      Socket s = new Socket("localhost", port);
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

class HelloService implements SocketServer {
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

class EchoService implements SocketServer {
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
