// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.socketservice.SocketServer;

public class FitNesseServer implements SocketServer {
  private static final Logger LOG = Logger.getLogger(FitNesseServer.class.getName());

  private final FitNesseContext context;
  private final ExecutorService executorService;

  public FitNesseServer(FitNesseContext context, ExecutorService executorService) {
    this.context = context;
    this.executorService = executorService;
  }

  @Override
  public void serve(Socket s) {
    serve(s, 10000);
  }

  public void serve(Socket s, long requestTimeout) {
    try {
      FitNesseExpediter sender = new FitNesseExpediter(s, context, executorService, requestTimeout);
      executorService.submit(sender);
    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, "Error while serving socket " + s, e);
    }
  }
}
