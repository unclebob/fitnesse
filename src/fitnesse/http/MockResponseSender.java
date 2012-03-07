// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.testutil.MockSocket;
import util.ConcurrentBoolean;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.Socket;

public class MockResponseSender implements ResponseSender {
  public MockSocket socket;
  protected ConcurrentBoolean closed;

  public MockResponseSender() {
    socket = new MockSocket("Mock");
    closed = new ConcurrentBoolean();
  }

  public void send(byte[] bytes) {
    try {
      socket.getOutputStream().write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    closed.set(true);
  }

  @Override
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

  public void waitForClose(long timeoutMillis) {
    if (!closed.waitFor(true, timeoutMillis))
      throw new RuntimeException("MockResponseSender could not be closed");
  }

  public boolean isClosed() {
    return closed.isTrue();
  }

  public static class OutputStreamSender extends MockResponseSender {
    public OutputStreamSender(OutputStream out) {
      socket = new MockSocket(new PipedInputStream(), out);
    }

    public void doSending(Response response) throws IOException {
      response.readyToSend(this);
      while (!closed.isTrue())
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // silently ignore
        }
    }
  }
}
