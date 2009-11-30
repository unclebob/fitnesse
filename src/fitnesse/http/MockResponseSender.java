// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.testutil.MockSocket;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.Socket;

public class MockResponseSender implements ResponseSender {
  public MockSocket socket;
  protected boolean closed = false;

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
    waitForClose(10000);
  }

  // Utility method that returns when this.closed is true. Throws an exception
  // if the timeout is reached.
  public synchronized void waitForClose(long timeoutMillis) throws Exception {
    while (!closed && timeoutMillis > 0) {
      wait(100);
      timeoutMillis -= 100;
    }
    if (!closed)
      throw new Exception("MockResponseSender could not be closed");
  }

  public boolean isClosed() {
    return closed;
  }

  public static class OutputStreamSender extends MockResponseSender {
    public OutputStreamSender(OutputStream out) {
      socket = new MockSocket(new PipedInputStream(), out);
    }

    public void doSending(Response response) throws Exception {
      response.readyToSend(this);
      while (!closed)
        Thread.sleep(1000);
    }
  }
}
