// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.MockRequestBuilder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.socketservice.SocketFactory;
import fitnesse.socketservice.SocketService;
import fitnesse.util.MockSocket;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FitNesse {
  private static final Logger LOG = Logger.getLogger(FitNesse.class.getName());
  private final FitNesseContext context;
  private boolean makeDirs = true;
  private volatile SocketService theService;

  public FitNesse(FitNesseContext context) {
    this.context = context;
  }

  public FitNesse dontMakeDirs() {
    makeDirs = false;
    return this;
  }

  private void establishRequiredDirectories() {
    establishDirectory(context.getRootPagePath());
    establishDirectory(context.getRootPagePath() + "/files");
  }

  private static void establishDirectory(String path) {
    File filesDir = new File(path);
    if (!filesDir.exists())
      filesDir.mkdir();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("DEPRECATED:  use java -jar fitnesse.jar or java -cp fitnesse.jar fitnesseMain.FitNesseMain");
    Class<?> mainClass = Class.forName("fitnesseMain.FitNesseMain");
    Method mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.invoke(null, new Object[]{args});
  }

  public boolean start() {
    if (makeDirs) {
      establishRequiredDirectories();
    }
    try {
      if (context.port > 0) {
        ServerSocket serverSocket = SocketFactory.tryCreateServerSocket(context.port, context.useHTTPS, context.sslClientAuth, context.sslParameterClassName);
        theService = new SocketService(new FitNesseServer(context), false, serverSocket);
      }
      return true;
    } catch (BindException e) {
      LOG.severe("FitNesse cannot be started...");
      LOG.severe("Port " + context.port + " is already in use.");
      LOG.severe("Use the -p <port#> command line argument to use a different port.");
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error while starting the FitNesse socket service", e);
    }
    return false;
  }

  public void stop() throws IOException {
    if (theService != null) {
      theService.close();
      theService = null;
    }
  }

  public boolean isRunning() {
    return theService != null;
  }

  public void executeSingleCommand(String command, OutputStream out) throws Exception {
    Request request = new MockRequestBuilder(command).noChunk().build();
    FitNesseExpediter expediter = new FitNesseExpediter(new MockSocket(), context);
    Response response = expediter.createGoodResponse(request);
    if (response.getStatus() != 200){
        throw new Exception("error loading page: " + response.getStatus());
    }
    response.withoutHttpHeaders();
    MockResponseSender sender = new MockResponseSender.OutputStreamSender(out);
    sender.doSending(response);
  }
}
