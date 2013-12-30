// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Map;

import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.CommandRunnerExecutionLog;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.MockCommandRunner;

public class CommandRunningFitClient extends FitClient implements SocketSeeker {
  public static int TIMEOUT = 60000;
  private static final String SPACE = " ";

  private SocketDoner donor;
  private boolean connectionEstablished = false;

  private final CommandRunningStrategy commandRunningStrategy;

  public CommandRunningFitClient(CommandRunningStrategy commandRunningStrategy) {
    super();
    this.commandRunningStrategy = commandRunningStrategy;
    commandRunningStrategy.init(this);
  }

  public void start() {
    try {
      commandRunningStrategy.start();
      waitForConnection();
    } catch (Exception e) {
      exceptionOccurred(e);
    }
  }

  public ExecutionLog getExecutionLog() {
    return new CommandRunnerExecutionLog(commandRunningStrategy.getCommandRunner());
  }

  @Override
  public void acceptSocketFrom(SocketDoner donor) throws IOException, InterruptedException {
    this.donor = donor;
    acceptSocket(donor.donateSocket());
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

  public void join() {
    commandRunningStrategy.join();
    super.join();
    if (donor != null)
      donor.finishedWithSocket();
    commandRunningStrategy.kill();
  }

  public void kill() {
    super.kill();
    commandRunningStrategy.kill();
  }

  public interface CommandRunningStrategy {
    void init(CommandRunningFitClient fitClient);

    void start() throws IOException;

    void join();

    void kill();

    CommandRunner getCommandRunner();
  }

  /** Runs commands by starting a new process. */
  public static class OutOfProcessCommandRunner implements CommandRunningStrategy {

    private final String command;
    private final Map<String, String> environmentVariables;
    private final int port;
    private final int ticketNumber;
    private Thread timeoutThread;
    private Thread earlyTerminationThread;
    private CommandRunner commandRunner;
    private CommandRunningFitClient fitClient;

    public OutOfProcessCommandRunner(String command, Map<String, String> environmentVariables, int port, int ticketNumber) {
      this.command = command;
      this.environmentVariables = environmentVariables;
      this.port = port;
      this.ticketNumber = ticketNumber;
    }

    @Override
    public void init(CommandRunningFitClient fitClient) {
      this.fitClient = fitClient;
      makeCommandRunner();
    }

    private void makeCommandRunner() {
      String fitArguments = getLocalhostName() + SPACE + port + SPACE + ticketNumber;
      String commandLine = command + SPACE + fitArguments;
      this.commandRunner = new CommandRunner(commandLine, "", environmentVariables);
    }

    @Override
    public void start() throws IOException {
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

    @Override
    public CommandRunner getCommandRunner() {
      return commandRunner;
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

      public void run() {
        try {
          Thread.sleep(1000); // next waitFor() can finish too quickly on Linux!
          commandRunner.waitForCommandToFinish();
          synchronized (fitClient) {
            if (!fitClient.isConnectionEstablished()) {
              fitClient.notify();
              fitClient.exceptionOccurred(new Exception(
                  "FitClient: external process terminated before a connection could be established."));
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
    private final int port;
    private final int ticketNumber;
    private Thread fastFitServer;
    private MockCommandRunner commandRunner;

    public InProcessCommandRunner(String testRunner, int port, int ticketNumber) {
      this.testRunner = testRunner;
      this.port = port;
      this.ticketNumber = ticketNumber;
    }

    @Override
    public void init(CommandRunningFitClient fitClient) {
      String[] arguments = new String[] { "-x", getLocalhostName(), Integer.toString(port), Integer.toString(ticketNumber) };
      this.fastFitServer = createTestRunnerThread(testRunner, arguments);
      this.fastFitServer.start();
      this.commandRunner = new MockCommandRunner();
    }

    @Override
    public void start() throws IOException {
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

    @Override
    public CommandRunner getCommandRunner() {
      return commandRunner;
    }

    protected Thread createTestRunnerThread(final String testRunner, final String[] args) {
      final Method testRunnerMethod = getTestRunnerMethod(testRunner);
      Runnable fastFitServerRunnable = new Runnable() {
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

  private static String getLocalhostName() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}