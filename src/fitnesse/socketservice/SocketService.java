// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketService {
  private static final Logger LOG = Logger.getLogger(SocketService.class.getName());

  private final ServerSocket serverSocket;
  private final Thread serviceThread;
  private volatile boolean running = false;
  private final SocketServer server;
  private final LinkedList<Thread> threads = new LinkedList<Thread>();
  private volatile boolean everRan = false;

  public SocketService(int port, SocketServer server) throws IOException {
    this(port, server, false);
  }

  public SocketService(int port, SocketServer server, boolean daemon) throws IOException {
    this.server = server;
    serverSocket = SocketFactory.tryCreateServerSocket(port);
    serviceThread = new Thread(
      new Runnable() {
        public void run() {
          serviceThread();
        }
      }
    );
    serviceThread.setDaemon(daemon);
    serviceThread.start();
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public void close() throws IOException {
    waitForServiceThreadToStart();
    running = false;
    serverSocket.close();
    try {
      serviceThread.join();
      waitForServerThreads();
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
    everRan = true;
    while (running) {
      try {
        Socket s = serverSocket.accept();
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
    Thread serverThread = new Thread(new ServerRunner(s));
    synchronized (threads) {
      threads.add(serverThread);
    }
    serverThread.start();
  }

  private void waitForServerThreads() throws InterruptedException {
    while (threads.size() > 0) {
      Thread t;
      synchronized (threads) {
        if (threads.size() < 1)
          return;
        t = threads.getFirst();
      }
      t.join();
    }
  }

  private class ServerRunner implements Runnable {
    private Socket socket;

    ServerRunner(Socket s) {
      socket = s;
    }

    public void run() {
      try {
        server.serve(socket);
        synchronized (threads) {
          threads.remove(Thread.currentThread());
        }
      } catch (Exception e) {
      }
    }
  }

}
