// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.socketservice.SocketService;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.MockCommandRunner;
import org.apache.commons.lang.ArrayUtils;

public class CommandRunningFitClient extends FitClient {
  private static final Logger LOG = Logger.getLogger(CommandRunningFitClient.class.getName());
  public static int TIMEOUT = 60000;

  private final int ticketNumber;
  private final CommandRunningStrategy commandRunningStrategy;
  private boolean connectionEstablished = false;
  private SocketService server;

  public CommandRunningFitClient(CommandRunningStrategy commandRunningStrategy) {
    super();
    this.ticketNumber = generateTicketNumber();
    this.commandRunningStrategy = commandRunningStrategy;
  }

  private int generateTicketNumber() {
    return 0xF17;
  }

  public void start() throws IOException {
    ServerSocket serverSocket = new PlainServerSocketFactory().createServerSocket(0);
    server = new SocketService(new SocketCatcher(this, ticketNumber), true, serverSocket);
    int port = serverSocket.getLocalPort();
    try {
      commandRunningStrategy.start(this, port, ticketNumber);
      waitForConnection();
    } catch (Exception e) {
      exceptionOccurred(e);
    }
  }

  @Override
  public void acceptSocket(Socket s) throws IOException, InterruptedException {
    super.acceptSocket(s);
    connectionEstablished = true;
    synchronized (this) {
      notify();
    }
  }

  private void waitForConnection() throws InterruptedException {
    while (!isSuccessfullyStarted()) {
      Thread.sleep(100);
      checkForPulse();
    }
  }

  public boolean isConnectionEstablished() {
    return connectionEstablished;
  }

  @Override
  public void join() {
    try {
      commandRunningStrategy.join();
      super.join();

      commandRunningStrategy.kill();
    } finally {
      closeServer();
    }
  }

  private void closeServer() {
    try {
      server.close();
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Unable to close FitClient socket server", e);
    }
  }

  @Override
  public void kill() {
    super.kill();
    commandRunningStrategy.kill();
  }

  public interface CommandRunningStrategy {
    void start(CommandRunningFitClient fitClient, int port, int ticketNumber) throws IOException;

    void join();

    void kill();
  }

  /** Runs commands by starting a new process. */
  public static class OutOfProcessCommandRunner implements CommandRunningStrategy {

    private final String[] command;
    private final Map<String, String> environmentVariables;
    private final ExecutionLogListener executionLogListener;
    private Thread timeoutThread;
    private Thread earlyTerminationThread;
    private CommandRunner commandRunner;

    public OutOfProcessCommandRunner(String[] command, Map<String, String> environmentVariables, ExecutionLogListener executionLogListener) {
      this.command = command;
      this.environmentVariables = environmentVariables;
      this.executionLogListener = executionLogListener;
    }

    private void makeCommandRunner(int port, int ticketNumber) throws UnknownHostException {
      String[] fitArguments = { getLocalhostName(), Integer.toString(port), Integer.toString(ticketNumber) };
      String[] commandLine = (String[]) ArrayUtils.addAll(command, fitArguments);
      commandRunner = new CommandRunner(commandLine, environmentVariables, executionLogListener);
    }

    @Override
    public void start(CommandRunningFitClient fitClient, int port, int ticketNumber) throws IOException {
      makeCommandRunner(port, ticketNumber);
      commandRunner.asynchronousStart();
      timeoutThread = new Thread(new TimeoutRunnable(fitClient), "FitClient timeout");
      timeoutThread.start();
      earlyTerminationThread = new Thread(new EarlyTerminationRunnable(fitClient, commandRunner), "FitClient early termination");
      earlyTerminationThread.start();
    }

    @Override
    public void join() {
      commandRunner.join();
      killVigilantThreads();
    }

    @Override
    public void kill() {
      commandRunner.kill();
      killVigilantThreads();
    }

    private void killVigilantThreads() {
      if (timeoutThread != null)
        timeoutThread.interrupt();
      if (earlyTerminationThread != null)
        earlyTerminationThread.interrupt();
    }

    private static class TimeoutRunnable implements Runnable {

      private final FitClient fitClient;

      public TimeoutRunnable(FitClient fitClient) {
        this.fitClient = fitClient;
      }

      @Override
      public void run() {
        try {
          Thread.sleep(TIMEOUT);
          synchronized (this.fitClient) {
            if (!fitClient.isSuccessfullyStarted()) {
              fitClient.notify();
              fitClient.exceptionOccurred(new Exception(
                  "FitClient: communication socket was not received on time."));
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt(); // remember interrupted
        }
      }
    }

    private static class EarlyTerminationRunnable implements Runnable {
      private final CommandRunningFitClient fitClient;
      private final CommandRunner commandRunner;

      EarlyTerminationRunnable(CommandRunningFitClient fitClient, CommandRunner commandRunner) {
        this.fitClient = fitClient;
        this.commandRunner = commandRunner;
      }

      @Override
      public void run() {
        try {
          Thread.sleep(1000); // next waitFor() can finish too quickly on Linux!
          commandRunner.waitForCommandToFinish();
          synchronized (fitClient) {
            if (!fitClient.isConnectionEstablished()) {
              fitClient.notify();
              Exception e = new Exception(
                      "FitClient: external process terminated before a connection could be established.");
              // TODO: use executionLogListener.exceptionOccurred(e)
              commandRunner.exceptionOccurred(e);
              fitClient.exceptionOccurred(e);
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt(); // remember interrupted
        }
      }
    }
  }

  /** Runs commands in fast mode (in-process). */
  public static class InProcessCommandRunner implements CommandRunningStrategy {
    private final String testRunner;
    private final ExecutionLogListener executionLogListener;
    private Thread fastFitServer;
    private MockCommandRunner commandRunner;

    public InProcessCommandRunner(String testRunner, ExecutionLogListener executionLogListener) {
      this.testRunner = testRunner;
      this.executionLogListener = executionLogListener;
    }

    @Override
    public void start(CommandRunningFitClient fitClient, int port, int ticketNumber) throws IOException {
      String[] arguments = new String[] { "-x", getLocalhostName(), Integer.toString(port), Integer.toString(ticketNumber) };
      this.fastFitServer = createTestRunnerThread(testRunner, arguments);
      this.fastFitServer.start();
      commandRunner = new MockCommandRunner(executionLogListener);
      commandRunner.asynchronousStart();
    }

    @Override
    public void join() {
      try {
        fastFitServer.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // remember interrupted
      }
    }

    @Override
    public void kill() {
      commandRunner.kill();
    }

    protected Thread createTestRunnerThread(final String testRunner, final String[] args) {
      final Method testRunnerMethod = getTestRunnerMethod(testRunner);
      Runnable fastFitServerRunnable = new Runnable() {
        @Override
        public void run() {
          tryCreateTestRunner(testRunnerMethod, args);
        }
      };
      Thread fitServerThread = new Thread(fastFitServerRunnable);
      fitServerThread.setDaemon(true);
      return fitServerThread;
    }

    private boolean tryCreateTestRunner(Method testRunnerMethod, String[] args) {
      try {
        testRunnerMethod.invoke(null, (Object) args);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    private Method getTestRunnerMethod(String testRunner) {
      try {
        return Class.forName(testRunner).getDeclaredMethod("main", String[].class);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }

  }

  private static String getLocalhostName() throws UnknownHostException {
    return java.net.InetAddress.getLocalHost().getHostName();
  }

}
