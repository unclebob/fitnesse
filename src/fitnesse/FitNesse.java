// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.http.MockRequestBuilder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.socketservice.SocketService;
import fitnesse.util.MockSocket;
import fitnesse.util.SerialExecutorService;

public class FitNesse {
  private static final Logger LOG = Logger.getLogger(FitNesse.class.getName());

  private final FitNesseContext context;
  private final ExecutorService executorService;
  private volatile SocketService theService;

  public FitNesse(FitNesseContext context) {
    this.context = context;
    RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOG.log(Level.WARNING, "Could not handle request. Thread pool is exhausted.");
      }
    };
    this.executorService = new ThreadPoolExecutor(5, 100, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2),
            new DaemonThreadFactory(), rejectionHandler);
  }

  public void start(ServerSocket serverSocket) throws IOException {
    theService = new SocketService(new FitNesseServer(context, executorService), false, serverSocket);
  }

  public synchronized void stop() throws IOException {
    if (theService != null) {
      theService.close();
      theService = null;
    }
    if (!executorService.isShutdown()) {
      executorService.shutdown();
    }
  }

  public boolean isRunning() {
    return theService != null;
  }

  public void executeSingleCommand(String command, OutputStream out) throws Exception {
    Request request = new MockRequestBuilder(command).noChunk().build();
    FitNesseExpediter expediter = new FitNesseExpediter(new MockSocket(), context, new SerialExecutorService());
    Response response = expediter.createGoodResponse(request);
    if (response.getStatus() != 200){
        throw new Exception("error loading page: " + response.getStatus());
    }
    response.withoutHttpHeaders();
    MockResponseSender sender = new MockResponseSender(out);
    sender.doSending(response);
  }

  /**
   * The default thread factory - creates daemon threads
   */
  static class DaemonThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DaemonThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() :
              Thread.currentThread().getThreadGroup();
      namePrefix = "server-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r,
              namePrefix + threadNumber.getAndIncrement(),
              0);
      t.setDaemon(true);
      if (t.getPriority() != Thread.NORM_PRIORITY)
        t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }

}
