// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.socketservice.SocketFactory;
import util.CommandLine;

import static fitnesse.slim.JavaSlimFactory.createJavaSlimFactory;

public class SlimService {
  public static final String OPTION_DESCRIPTOR = "[-v] [-i interactionClass] [-s statementTimeout] [-d] [-ssl parameterClass] port";
  static FixtureInteraction interaction = getInteraction(null);

  public static class Options {
    final boolean verbose;
    final int port;
    final FixtureInteraction interaction;
    /**
     * daemon mode: keep accepting new connections indefinitely.
     */
    final boolean daemon;
    final Integer statementTimeout;
    final boolean useSSL;
	final String sslParameterClassName;

    public Options(boolean verbose, int port, FixtureInteraction interaction, boolean daemon, Integer statementTimeout, boolean useSSL, String sslParameterClassName) {
      this.verbose = verbose;
      this.port = port;
      this.interaction = interaction;
      this.daemon = daemon;
      this.statementTimeout = statementTimeout;
      this.useSSL = useSSL;
      this.sslParameterClassName = sslParameterClassName;
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
    	try{
    		startWithFactory(createJavaSlimFactory(options), options);
		System.exit(0);
    	}catch (Exception e){
    		e.printStackTrace();
    		System.out.println("Exiting as exception occured: " + e.getMessage());
    		System.exit(98);
    	}
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
    SlimService slimservice = new SlimService(slimFactory.getSlimServer(options.verbose), options.port, options.interaction, options.daemon, options.useSSL, options.sslParameterClassName);
    slimservice.accept();
  }

  // For testing only -- for now
  public static synchronized int startWithFactoryAsync(SlimFactory slimFactory, Options options) throws IOException {
    if (service != null && service.isAlive()) {
      service.interrupt();
      throw new SlimError("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
    }
    final SlimService slimservice = new SlimService(slimFactory.getSlimServer(options.verbose), options.port, options.interaction, options.daemon, options.useSSL, options.sslParameterClassName);
    int actualPort = slimservice.getPort();
    service = new Thread() {
      public void run() {
        try {
          slimservice.accept();
        } catch (IOException e) {
          throw new SlimError(e);
        }
      }
    };
    service.start();
    return actualPort;
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
      String sslParameterClassName = commandLine.getOptionArgument("ssl", "parameterClass");
      boolean useSSL = commandLine.hasOption("ssl");
      return new Options(verbose, port, getInteraction(interactionClassName), daemon, statementTimeout, useSSL, sslParameterClassName);
    }
    return null;
  }

  public SlimService(SlimServer slimServer, int port, FixtureInteraction interaction, boolean daemon, boolean useSSL, String sslParameterClassName) throws IOException {
    SlimService.interaction = interaction;
    this.daemon = daemon;
    this.slimServer = slimServer;

    try {
      serverSocket = SocketFactory.tryCreateServerSocket(port, useSSL, useSSL, sslParameterClassName);
    } catch (java.lang.OutOfMemoryError e) {
      System.err.println("Out of Memory. Aborting.");
      e.printStackTrace();
      System.exit(99);
      throw e;
    } catch (BindException e) {
      System.err.println("Can not bind to port " + port + ". Aborting.");
      e.printStackTrace();
      throw e;
    }
  }

  public int getPort() {
    return serverSocket.getLocalPort();
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
            throw new SlimError(e);
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
  private static FixtureInteraction getInteraction(String interactionClassName) {
    if (interactionClassName == null) {
      return new DefaultInteraction();
    }
    try {
      return ((Class<FixtureInteraction>) Class.forName(interactionClassName)).newInstance();
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

  public static FixtureInteraction getInteraction() {
    return interaction;
  }
}
