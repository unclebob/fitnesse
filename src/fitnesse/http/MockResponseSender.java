// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.testutil.MockSocket;

import java.io.IOException;
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

  public void send(byte[] bytes) {
    try {
      socket.getOutputStream().write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    closed = true;
    notifyAll();
  }

  public Socket getSocket() {
    return socket;
  }

  public String sentData() {
    return socket.getOutput();
  }

  public void doSending(Response response) throws IOException {
    response.readyToSend(this);
    waitForClose(20000);
  }

  // Utility method that returns when this.closed is true. Throws an exception
  // if the timeout is reached.
  public void waitForClose(long timeoutMillis) {
    while (!closed && timeoutMillis > 0) {
      try {
        wait(100);
      } catch (InterruptedException e) {
        // Fall through. Log?
        e.printStackTrace();
      }
      timeoutMillis -= 100;
    }
    if (!closed)
      throw new RuntimeException("MockResponseSender could not be closed");
  }

  public boolean isClosed() {
    return closed;
  }

  public static class OutputStreamSender extends MockResponseSender {
    public OutputStreamSender(OutputStream out) {
      socket = new MockSocket(new PipedInputStream(), out);
    }

    public void doSending(Response response) throws IOException {
      response.readyToSend(this);
      try {
        while (!closed)
          Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
