// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.IOException;
import java.net.Socket;

import fitnesse.socketservice.SocketServer;
import fitnesse.socketservice.SocketServerShutdownException;

public class FitNesseServer implements SocketServer {
  private FitNesseContext context;

  public FitNesseServer(FitNesseContext context) {
    this.context = context;
  }

  public void serve(Socket s) throws SocketServerShutdownException {
    serve(s, 10000);
  }

  public void serve(Socket s, long requestTimeout) throws SocketServerShutdownException {
    try {
      FitNesseExpediter sender = new FitNesseExpediter(s, context);
      sender.setRequestParsingTimeLimit(requestTimeout);
      sender.start();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
