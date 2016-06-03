// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fit.Counts;
import fit.FitProtocol;
import fitnesse.testsystems.TestSummary;
import util.StreamReader;

public class FitClient implements SocketAccepter {
  private static final Logger LOG = Logger.getLogger(FitClient.class.getName());

  private List<FitClientListener> listeners;
  private Socket fitSocket;
  private OutputStream fitInput;
  private StreamReader fitOutput;

  private volatile int sent = 0;
  private volatile int received = 0;
  private volatile boolean isDoneSending = false;
  private volatile boolean killed = false;
  private Thread fitListeningThread;

  public FitClient() {
    this.listeners = new LinkedList<>();
  }

  public void addFitClientListener(FitClientListener listener) {
    listeners.add(listener);
  }

  @Override
  public synchronized void acceptSocket(Socket socket) throws IOException, InterruptedException {
    checkForPulse();
    fitSocket = socket;
    fitInput = fitSocket.getOutputStream();
    FitProtocol.writeData("", fitInput);
    fitOutput = new StreamReader(fitSocket.getInputStream());

    fitListeningThread = new Thread(new FitListeningRunnable(), "FitClient fitOutput");
    fitListeningThread.start();
  }

  public void send(String data) throws IOException, InterruptedException {
    checkForPulse();
    FitProtocol.writeData(data, fitInput);
    sent++;
  }

  public void done() throws IOException, InterruptedException {
    checkForPulse();
    FitProtocol.writeSize(0, fitInput);
    isDoneSending = true;
  }

  public void join() {
    if (fitListeningThread != null)
      try {
        fitListeningThread.join();
      } catch (InterruptedException e) {
        LOG.log(Level.FINE, "Wait for join of listening thread interrupted");
        Thread.currentThread().interrupt();
      }
  }

  public void kill() {
    killed = true;
    if (fitListeningThread != null)
      fitListeningThread.interrupt();
  }

  public synchronized boolean isSuccessfullyStarted() {
    return fitSocket != null;
  }

  protected void checkForPulse() throws InterruptedException {
    if (killed)
      throw new InterruptedException("FitClient was killed");
  }

  private boolean finishedReading() throws InterruptedException {
    while (stateIndeterminate())
      shortSleep();
    return isDoneSending && received == sent;
  }

  /**
   * @return true if the current state of the transission is indeterminate.
   *         <p/>
   *         When the number of pages sent and recieved is the same, we may be done with the whole job, or we may just
   *         be waiting for FitNesse to send the next page. There's no way to know until FitNesse either calls send, or
   *         done.
   */
  private boolean stateIndeterminate() {
    return (received == sent) && !isDoneSending;
  }

  private void shortSleep() throws InterruptedException {
    Thread.sleep(10);
  }

  public void exceptionOccurred(Throwable cause) {
    for (FitClientListener listener : listeners)
      listener.exceptionOccurred(cause);
  }

  private class FitListeningRunnable implements Runnable {
    @Override
    public void run() {
      listenToFit();
    }

    private void listenToFit() {
      try {
        attemptToListenToFit();
      } catch (Exception e) {
         exceptionOccurred(e);
      }
    }

    private void attemptToListenToFit() throws IOException, InterruptedException {
      while (!finishedReading()) {
        int size;
        size = FitProtocol.readSize(fitOutput);
        if (size != 0) {
          String readValue = fitOutput.read(size);
          if (fitOutput.byteCount() < size)
            throw new IOException("I was expecting " + size + " bytes but I only got " + fitOutput.byteCount());
          testOutputChunk(readValue);
        } else {
          Counts counts = FitProtocol.readCounts(fitOutput);
          testComplete(new TestSummary(counts.right, counts.wrong, counts.ignores, counts.exceptions));
          received++;
        }
      }
    }

    private void testComplete(TestSummary summary) throws IOException {
      for (FitClientListener listener : listeners)
        listener.testComplete(summary);
    }

    private void testOutputChunk(String value) throws IOException {
      for (FitClientListener listener : listeners)
        listener.testOutputChunk(value);
    }
  }

}
