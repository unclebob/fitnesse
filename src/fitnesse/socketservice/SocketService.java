// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs a separate service (thread) to handle new connections.
 */
public class SocketService {
  private static final Logger LOG = Logger.getLogger(SocketService.class.getName());

  private final ServerSocket serverSocket;
  private final Thread serviceThread;
  private volatile boolean running = false;
  private final SocketServer server;

  private volatile boolean everRan = false;

  public SocketService(SocketServer server, boolean daemon, ServerSocket serverSocket) throws IOException {
    this.server = server;
    this.serverSocket = serverSocket;
    serviceThread = new Thread(
      new Runnable() {
        @Override
        public void run() {
          serviceThread();
        }
      }
    );
    serviceThread.setDaemon(daemon);
    serviceThread.start();
  }

  public void close() throws IOException {
    waitForServiceThreadToStart();
    running = false;
    serverSocket.close();
    try {
      serviceThread.join();
    } catch (InterruptedException e) {
      LOG.log(Level.WARNING, "Thread joining interrupted");
      Thread.currentThread().interrupt();
    }
  }

  private void waitForServiceThreadToStart() {
    if (everRan) return;
    while (!running) Thread.yield();
  }

  private void serviceThread() {
    running = true;
    while (running) {
      try {
        Socket s = serverSocket.accept();
        everRan = true;
        server.serve(s);
      } catch (java.lang.OutOfMemoryError e) {
        LOG.log(Level.SEVERE, "Can't create new thread.  Out of Memory.  Aborting.", e);
        System.exit(99);
      } catch (SocketException sox) {
        running = false;
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "I/O exception in service thread", e);
      }
    }
  }
}
