// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.File;
import java.lang.reflect.Method;
import java.net.BindException;

import fitnesse.socketservice.SocketService;

public class FitNesse {
  private FitNesseContext context;
  private SocketService theService;
  private Updater updater;
  public static final FitNesseVersion VERSION = new FitNesseVersion();

  public static void main(String[] args) throws Exception {
    System.out.println("DEPRECATED:  use java -jar fitnesse.jar or java -cp fitnesse.jar fitnesseMain.FitNesseMain");
    Class<?> mainClass = Class.forName("fitnesseMain.FitNesseMain");
    Method mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.invoke(null, new Object[]{args});
  }

  private static void printBadPortMessage(int port) {
    System.err.println("FitNesse cannot be started...");
    System.err.println("Port " + port + " is already in use.");
    System.err.println("Use the -p <port#> command line argument to use a different port.");
  }

  private static void establishDirectory(String path) {
    File filesDir = new File(path);
    if (!filesDir.exists())
      filesDir.mkdir();
  }

  public FitNesse(FitNesseContext context) {
    this(context, null, true);
  }

  public FitNesse(FitNesseContext context, Updater updater) {
    this(context, updater, true);
  }

  public FitNesse(FitNesseContext context, boolean makeDirs) {
    this(context, null, makeDirs);
  }


  // TODO MdM. This boolean agument is annoying... please fix.
  public FitNesse(FitNesseContext context, Updater updater, boolean makeDirs) {
    this.updater = updater;
    this.context = context;
    context.fitnesse = this;
    FitNesseContext.globalContext = context;
    if (makeDirs)
      establishRequiredDirectories();
  }

  public boolean start() {
    try {
      theService = new SocketService(context.port, new FitNesseServer(context));
      return true;
    } catch (BindException e) {
      printBadPortMessage(context.port);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public void stop() throws Exception {
    if (theService != null) {
      theService.close();
      theService = null;
    }
  }

  private void establishRequiredDirectories() {
    establishDirectory(context.rootPagePath);
    establishDirectory(context.rootPagePath + "/files");
  }

  public void applyUpdates() throws Exception {
    if (updater != null)
      updater.update();
  }


  public boolean isRunning() {
    return theService != null;
  }

  public FitNesseContext getContext() {
    return context;
  }
}
