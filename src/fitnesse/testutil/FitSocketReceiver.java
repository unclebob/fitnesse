// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import fitnesse.http.Request;
import fitnesse.responders.run.SocketDealer;

public class FitSocketReceiver {
  public static int DEFAULT_SOCKET = 9123;

  public ServerSocket serverSocket;
  public Socket socket;
  public int port = DEFAULT_SOCKET;
  public SocketDealer dealer;

  public FitSocketReceiver(int port, SocketDealer dealer) {
    this.port = port;
    this.dealer = dealer;
  }

  public int receiveSocket() throws Exception {
    serverSocket = new ServerSocket(port);
    port = serverSocket.getLocalPort();
    new Thread() {
      public void run() {
        try {
          socket = serverSocket.accept();
          serverSocket.close();
          Request request = new Request(socket.getInputStream());
          request.parse();

          int ticket = Integer.parseInt(request.getInput("ticket").toString());
          dealSocket(ticket);
        }
        catch (SocketException se) {
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.start();
    return port;
  }

  protected void dealSocket(int ticket) throws Exception {
    dealer.dealSocketTo(ticket, new SimpleSocketDoner(socket));
  }

  public void close() throws Exception {
    if (serverSocket != null)
      serverSocket.close();
    if (socket != null)
      socket.close();
  }
}
