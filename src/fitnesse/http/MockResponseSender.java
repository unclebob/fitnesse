// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.net.Socket;

import fitnesse.testutil.MockSocket;

public class MockResponseSender implements ResponseSender {
  public MockSocket socket;
  private boolean closed = false;

  public MockResponseSender() {
    socket = new MockSocket("Mock");
    closed = false;
  }

  public void send(byte[] bytes) throws Exception {
    //Todo Timing Problem -- Figure out why this is necessary.  
    for (int i = 0; i < 1000; i++) Thread.yield();
    socket.getOutputStream().write(bytes);
  }

  public synchronized void close() throws Exception {
    closed = true;
    notifyAll();
  }

  public Socket getSocket() throws Exception {
    return socket;
  }

  public String sentData() throws Exception {
    return socket.getOutput();
  }

  public void doSending(Response response) throws Exception {
    response.readyToSend(this);
    waitForClose(5000);
  }

  // Utility method that returns when this.closed is true. Throws an exception
  // if the timeout is reached.
  public synchronized void waitForClose(final long timeoutMillis) throws Exception {
    if (!closed) {
      wait(timeoutMillis);
      if (!closed)
        throw new Exception("MockResponseSender could not be closed");
    }
  }

  public boolean isClosed() {
    return closed;
  }
}
