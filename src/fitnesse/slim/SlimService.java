// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import util.CommandLine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fitnesse.socketservice.SocketFactory;

import static fitnesse.slim.JavaSlimFactory.createJavaSlimFactory;

public class SlimService {
  public static final String OPTION_DESCRIPTOR = "[-v] [-i interactionClass] [-s statementTimeout] [-d] port";
  static Class<? extends DefaultInteraction> interactionClass = DefaultInteraction.class;

  public static class Options {
    final boolean verbose;
    final int port;
    final Class<? extends DefaultInteraction> interactionClass;
    /**
     * daemon mode: keep accepting new connections indefinitely.
     */
    final boolean daemon;
    final Integer statementTimeout;

    public Options(boolean verbose, int port, Class<? extends DefaultInteraction> interactionClass, boolean daemon, Integer statementTimeout) {
      this.verbose = verbose;
      this.port = port;
      this.interactionClass = interactionClass;
      this.daemon = daemon;
      this.statementTimeout = statementTimeout;
    }
  }

  private final ServerSocket serverSocket;
  private final SlimServer slimServer;
  private final boolean daemon;
  private final Executor executor = Executors.newFixedThreadPool(5);
  static Thread service;

  public static void main(String[] args) throws IOException {
    Options options = parseCommandLine(args);
    if (options != null) {
      startWithFactory(createJavaSlimFactory(options), options);
    } else {
      parseCommandLineFailed(args);
    }
  }

  protected static void parseCommandLineFailed(String[] args) {
    System.err.println("Invalid command line arguments: " + Arrays.asList(args));
    System.err.println("Usage:");
    System.err.println("    " + SlimService.class.getName() + " " + OPTION_DESCRIPTOR);
  }

  public static void startWithFactory(SlimFactory slimFactory, Options options) throws IOException {
    SlimService slimservice = new SlimService(slimFactory.getSlimServer(options.verbose), options.port, options.interactionClass, options.daemon);
    slimservice.accept();
  }

  // For testing only -- for now
  public static synchronized void startWithFactoryAsync(SlimFactory slimFactory, Options options) throws IOException {
    if (service != null && service.isAlive()) {
      System.err.println("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
      service.interrupt();
      throw new RuntimeException("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
    }
    final SlimService slimservice = new SlimService(slimFactory.getSlimServer(options.verbose), options.port, options.interactionClass, options.daemon);
    service = new Thread() {
      public void run() {
        try {
          slimservice.accept();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
    service.start();
  }

  // For testing, mainly.
  public static void waitForServiceToStopAsync() throws InterruptedException {
    // wait for service to close.
    for (int i = 0; i < 1000; i++) {
      if (!service.isAlive())
        break;
      Thread.sleep(50);
    }
  }

  public static Options parseCommandLine(String[] args) {
    CommandLine commandLine = new CommandLine(OPTION_DESCRIPTOR);
    if (commandLine.parse(args)) {
      boolean verbose = commandLine.hasOption("v");
      String interactionClassName = commandLine.getOptionArgument("i", "interactionClass");
      String portString = commandLine.getArgument("port");
      int port = (portString == null) ? 8099 : Integer.parseInt(portString);
      String statementTimeoutString = commandLine.getOptionArgument("s", "statementTimeout");
      Integer statementTimeout = (statementTimeoutString == null) ? null : Integer.parseInt(statementTimeoutString);
      boolean daemon = commandLine.hasOption("d");
      return new Options(verbose, port, getInteractionClass(interactionClassName), daemon, statementTimeout);
    }
    return null;
  }

  public SlimService(SlimServer slimServer, int port, Class<? extends DefaultInteraction> interactionClass, boolean daemon) throws IOException {
    SlimService.interactionClass = interactionClass;
    this.daemon = daemon;
    this.slimServer = slimServer;

    try {
      serverSocket = SocketFactory.tryCreateServerSocket(port);
    } catch (java.lang.OutOfMemoryError e) {
      System.err.println("Out of Memory. Aborting");
      e.printStackTrace();
      System.exit(99);

      throw e;
    }
  }

  public void accept() throws IOException {
    try {
      if (daemon) {
        acceptMany();
      } else {
        acceptOne();
      }
    } catch (java.lang.OutOfMemoryError e) {
      System.err.println("Out of Memory. Aborting");
      e.printStackTrace();
      System.exit(99);
    } finally {
      serverSocket.close();
    }
  }

  private void acceptMany() throws IOException {
    while (true) {
      final Socket socket = serverSocket.accept();
      executor.execute(new Runnable() {

        @Override
        public void run() {
          try {
            handle(socket);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }
  }

  private void handle(Socket socket) throws IOException {
    try {
      slimServer.serve(socket);
    } finally {
      socket.close();
    }
  }

  private void acceptOne() throws IOException {
    Socket socket = serverSocket.accept();
    handle(socket);
  }

  @SuppressWarnings("unchecked")
  private static Class<DefaultInteraction> getInteractionClass(String interactionClassName) {
    if (interactionClassName == null) {
      return DefaultInteraction.class;
    }
    try {
      return (Class<DefaultInteraction>) Class.forName(interactionClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static Class<? extends DefaultInteraction> getInteractionClass() {
    return interactionClass;
  }
}
