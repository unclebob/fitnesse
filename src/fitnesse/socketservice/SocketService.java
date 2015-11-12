// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketService {
  private static final Logger LOG = Logger.getLogger(SocketService.class.getName());

  private final ServerSocket serverSocket;
  private final Thread serviceThread;
  private volatile boolean running = false;
  private final SocketServer server;

  private final ExecutorService executorService = new ForkJoinPool();
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
      executorService.shutdown();
      executorService.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.log(Level.WARNING, "Thread joining interrupted", e);
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
        if (!everRan){
          // Print information about the first connection done
          SocketFactory.printSocketInfo(s);
        }
        everRan = true;
        startServerThread(s);
      } catch (java.lang.OutOfMemoryError e) {
        LOG.log(Level.SEVERE, "Can't create new thread.  Out of Memory.  Aborting.", e);
        System.exit(99);
      } catch (SocketException sox) {
        running = false;// do nothing
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void startServerThread(Socket s) {
    executorService.submit(new ServerRunner(s));
  }

  private class ServerRunner implements Runnable {
    private Socket socket;

    ServerRunner(Socket s) {
      socket = s;
    }

    @Override
    public void run() {
      try {
        server.serve(socket);
      } catch (Exception e) {
        LOG.log(Level.FINE, "Exception thrown while handling server request", e);
      }
    }
  }

}
