// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.MockRequestBuilder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.socketservice.SocketService;
import fitnesse.util.MockSocket;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.BindException;

public class FitNesse {
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
        theService = new SocketService(context.port, new FitNesseServer(context));
      }
      return true;
    } catch (BindException e) {
      printBadPortMessage(context.port);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private static void printBadPortMessage(int port) {
    System.err.println("FitNesse cannot be started...");
    System.err.println("Port " + port + " is already in use.");
    System.err.println("Use the -p <port#> command line argument to use a different port.");
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
    response.withoutHttpHeaders();
    MockResponseSender sender = new MockResponseSender.OutputStreamSender(out);
    sender.doSending(response);
  }
}
