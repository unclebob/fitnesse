// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static fitnesse.slim.JavaSlimFactory.createJavaSlimFactory;

import java.io.IOException;
import java.net.BindException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import util.CommandLine;
import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public class SlimSlave {
  private static final String OPTION_DESCRIPTOR = "[-v] [-i interactionClass] [-s statementTimeout] [-ssl parameterClass] port";

  public static class Options {
    public final boolean verbose;
    public final int port;
    public final FixtureInteraction interaction;
    /**
     * daemon mode: keep accepting new connections indefinitely.
     */
    public final boolean daemon;
    public final Integer statementTimeout;
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

  private final SlimSocket serverSocket;
  private final SlimServer slimServer;
  private final boolean daemon;
  private final Executor executor = Executors.newFixedThreadPool(5);

  public static void main(String[] args) throws IOException {
    Options options = parseCommandLine(args);
    if (options != null) {
      try {
        startWithFactory(
            createJavaSlimFactory(options.interaction,
                options.statementTimeout, options.verbose), options);
        System.exit(0);
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Exiting as exception occured: " + e.getMessage());
        System.exit(98);
      }
    } else {
      parseCommandLineFailed(args);
    }
  }

  protected static void parseCommandLineFailed(String[] args) {
    System.err.println("Invalid command line arguments: " + Arrays.asList(args));
    System.err.println("Usage:");
    System.err.println("    " + SlimSlave.class.getName() + " " + OPTION_DESCRIPTOR);
  }

  public static void startWithFactory(SlimFactory slimFactory, Options options) throws IOException {
    try {
      SlimSlave slimservice = new SlimSlave(slimFactory.getSlimServer(),
          new SlimSlaveSocket(), options.daemon);
      slimservice.accept();
    } catch (java.lang.OutOfMemoryError e) {
      System.err.println("Out of Memory. Aborting.");
      e.printStackTrace();
      System.exit(99);
      throw e;
    } catch (BindException e) {
      System.err.println("Can not bind to port " + options.port + ". Aborting.");
      e.printStackTrace();
      throw e;
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
      return new Options(verbose, port, createInteraction(interactionClassName), daemon, statementTimeout, useSSL, sslParameterClassName);
    }
    return null;
  }

  public SlimSlave(SlimServer slimServer, SlimSocket serverSocket,
      boolean daemon) throws IOException {
    this.daemon = daemon;
    this.slimServer = slimServer;
    this.serverSocket = serverSocket;
//
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
      final SlimSocket socket = serverSocket.accept();
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

  private void handle(SlimSocket socket) throws IOException {
    try {
      slimServer.serve(socket);
    } finally {
      socket.close();
    }
  }

  private void acceptOne() throws IOException {
    SlimSocket socket = serverSocket.accept();
    handle(socket);
  }

  @SuppressWarnings("unchecked")
  private static FixtureInteraction createInteraction(String interactionClassName) {
    if (interactionClassName == null) {
      return new DefaultInteraction();
    }
    try {
      return ((Class<FixtureInteraction>) Class.forName(interactionClassName)).newInstance();
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }
}
