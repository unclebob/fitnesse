// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import fit.FitServer;
import fitnesse.components.CommandRunner;
import fitnesse.components.FitClient;
import fitnesse.components.SocketDealer;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testutil.MockCommandRunner;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

public class CommandRunningFitClient extends FitClient implements SocketSeeker {
  public static int TIMEOUT = 60000;
  private static final String SPACE = " ";

  private final int ticketNumber;
  private final boolean fastTest;
  public final CommandRunner commandRunner;

  private SocketDoner donor;
  private boolean connectionEstablished = false;

  private Thread timeoutThread;
  private Thread earlyTerminationThread;
  private Thread fastFitServer;

  public CommandRunningFitClient(TestSystemListener listener, String command, int port, Map<String, String> environmentVariables, SocketDealer dealer, boolean fastTest) {
    super(listener);
    this.fastTest = fastTest;
    ticketNumber = dealer.seekingSocket(this);
    String hostName;
    try {
      hostName = java.net.InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    String fitArguments = hostName + SPACE + port + SPACE + ticketNumber;
    String commandLine = command + SPACE + fitArguments;
    if (fastTest) {
      commandRunner = new MockCommandRunner();
      createFitServer("-x " + fitArguments);
    } else
      commandRunner = new CommandRunner(commandLine, "", environmentVariables);
  }

  public CommandRunningFitClient(TestSystemListener listener, String command, int port, SocketDealer dealer) throws Exception {
    this(listener, command, port, null, dealer);
  }

  public CommandRunningFitClient(TestSystemListener listener, String command, int port, Map<String, String> environmentVariables, SocketDealer dealer) throws Exception {
    this(listener, command, port, environmentVariables, dealer, false);
  }

  //For testing only.  Makes responder faster.
  void createFitServer(String args) {
    final String fitArgs = args;
    Runnable fastFitServerRunnable = new Runnable() {
      public void run() {
        try {
          while (!tryCreateFitServer(fitArgs))
            Thread.sleep(10);
        } catch (Exception e) {

        }
      }
    };
    fastFitServer = new Thread(fastFitServerRunnable);
    fastFitServer.start();
  }

  private boolean tryCreateFitServer(String args) {
    try {
      FitServer.main(args.trim().split(" "));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void start() {
    try {
      commandRunner.asynchronousStart();
      if (!fastTest) {
        timeoutThread = new Thread(new TimeoutRunnable(), "FitClient timeout");
        timeoutThread.start();
        earlyTerminationThread = new Thread(new EarlyTerminationRunnable(), "FitClient early termination");
        earlyTerminationThread.start();
      }
      waitForConnection();
    }
    catch (Exception e) {
      listener.exceptionOccurred(e);
    }
  }

  public void acceptSocketFrom(SocketDoner donor) throws IOException, InterruptedException {
    this.donor = donor;
    acceptSocket(donor.donateSocket());
    connectionEstablished = true;

    synchronized (this) {
      notify();
    }
  }

  public boolean isSuccessfullyStarted() {
    return fitSocket != null;
  }

  private void waitForConnection() throws InterruptedException {
    while (fitSocket == null) {
      Thread.sleep(100);
      checkForPulse();
    }
  }

  public void join() {
    try {
      if (fastTest) {
        fastFitServer.join();
      } else {
        commandRunner.join();
      }
      super.join();
      if (donor != null)
        donor.finishedWithSocket();
      killVigilantThreads();
    }
    catch (InterruptedException e) {
    }
  }

  public void kill() {
    super.kill();
    killVigilantThreads();
    commandRunner.kill();
  }

  private void killVigilantThreads() {
    if (timeoutThread != null)
      timeoutThread.interrupt();
    if (earlyTerminationThread != null)
      earlyTerminationThread.interrupt();
  }

  public void exceptionOccurred(Exception e) {
    commandRunner.exceptionOccurred(e);
    super.exceptionOccurred(e);
  }

  private class TimeoutRunnable implements Runnable {

    public void run() {
      try {
        Thread.sleep(TIMEOUT);
        synchronized (CommandRunningFitClient.this) {
          if (fitSocket == null) {
            CommandRunningFitClient.this.notify();
            listener.exceptionOccurred(new Exception(
              "FitClient: communication socket was not received on time."));
          }
        }
      }
      catch (InterruptedException e) {
        // ok
      }
    }
  }

  private class EarlyTerminationRunnable implements Runnable {
    public void run() {
      try {
        Thread.sleep(1000);  // next waitFor() can finish too quickly on Linux!
        commandRunner.waitForCommandToFinish();
        synchronized (CommandRunningFitClient.this) {
          if (!connectionEstablished) {
            CommandRunningFitClient.this.notify();
            listener.exceptionOccurred(new Exception(
              "FitClient: external process terminated before a connection could be established."));
          }
        }
      }
      catch (InterruptedException e) {
        // ok
      }
    }
  }
}