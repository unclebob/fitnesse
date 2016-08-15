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
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.socketservice.ServerSocketFactory;
import fitnesse.socketservice.SslServerSocketFactory;
import util.CommandLine;

import static fitnesse.slim.JavaSlimFactory.createJavaSlimFactory;

public class SlimService {
  private static final int EXIT_CODE_EXCEPTION_ON_START = 98;
  private static final int EXIT_CODE_OUT_OF_MEMORY = 99;
  private static final int DEFAULT_PORT = 8099;
  private static final String OPTION_DESCRIPTOR = "[-v] [-i interactionClass] [-nt nameTranslatorClass] [-s statementTimeout] [-d] [-ssl parameterClass] port";

  public static class Options {
    public final boolean verbose;
    public final int port;
    public final FixtureInteraction interaction;
    public final NameTranslator nameTranslator;
    /**
     * daemon mode: keep accepting new connections indefinitely.
     */
    public final boolean daemon;
    public final Integer statementTimeout;
    final boolean useSSL;
    final String sslParameterClassName;

    public Options(boolean verbose, int port, FixtureInteraction interaction, NameTranslator nameTranslator, boolean daemon, Integer statementTimeout,
        boolean useSSL, String sslParameterClassName) {
      this.verbose = verbose;
      this.port = port;
      this.interaction = interaction;
      this.nameTranslator = nameTranslator;
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

  public static void main(String[] args) throws IOException {
    Options options = parseCommandLine(args);
    if (options != null) {
      try {
        startWithFactory(createJavaSlimFactory(options), options);
        System.exit(0);
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Exiting as exception occured: " + e.getMessage());
        System.exit(EXIT_CODE_EXCEPTION_ON_START);
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
    ServerSocketFactory serverSocketFactory = options.useSSL ? new SslServerSocketFactory(true, options.sslParameterClassName) : new PlainServerSocketFactory();
    try {
      SlimService slimservice = new SlimService(slimFactory.getSlimServer(), serverSocketFactory.createServerSocket(options.port), options.daemon);
      slimservice.accept();
    } catch (java.lang.OutOfMemoryError e) {
      System.err.println("Out of Memory. Aborting.");
      e.printStackTrace();
      System.exit(EXIT_CODE_OUT_OF_MEMORY);
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
      String nameTranslatorClassName = commandLine.getOptionArgument("nt", "nameTranslatorClass");
      String portString = commandLine.getArgument("port");
      int port = (portString == null) ? DEFAULT_PORT : Integer.parseInt(portString);
      String statementTimeoutString = commandLine.getOptionArgument("s", "statementTimeout");
      Integer statementTimeout = (statementTimeoutString == null) ? null : Integer.parseInt(statementTimeoutString);
      boolean daemon = commandLine.hasOption("d");
      String sslParameterClassName = commandLine.getOptionArgument("ssl", "parameterClass");
      boolean useSSL = commandLine.hasOption("ssl");
      return new Options(verbose, port, createInteraction(interactionClassName), createNameTranslator(nameTranslatorClassName), daemon, statementTimeout,
          useSSL, sslParameterClassName);
    }
    return null;
  }

  public SlimService(SlimServer slimServer, ServerSocket serverSocket, boolean daemon) throws IOException {
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
      System.exit(EXIT_CODE_OUT_OF_MEMORY);
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

  private static FixtureInteraction createInteraction(String interactionClassName) {
    return SlimService.<FixtureInteraction>createInstance(interactionClassName, new DefaultInteraction());
  }

  private static NameTranslator createNameTranslator(String nameTranslatorClassName) {
    return SlimService.<NameTranslator>createInstance(nameTranslatorClassName, new NameTranslatorIdentity());
  }

  @SuppressWarnings("unchecked")
  private static <T> T createInstance(String className, T defaultInstance) {
    if (className == null) {
      return defaultInstance;
    }
    try {
      return ((Class<T>) Class.forName(className)).newInstance();
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

}
