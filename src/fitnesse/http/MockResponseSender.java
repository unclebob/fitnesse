// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.testutil.MockSocket;
import util.ConcurrentBoolean;

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

  public void send(byte[] bytes) throws Exception {
    socket.getOutputStream().write(bytes);
  }

  public void close() throws Exception {
    closed.set(true);
  }

  public Socket getSocket() throws Exception {
    return socket;
  }

  public String sentData() throws Exception {
    return socket.getOutput();
  }

  public void doSending(Response response) throws Exception {
    response.readyToSend(this);
    waitForClose(20000);
  }

  public void waitForClose(long timeoutMillis) throws Exception {
    if (!closed.waitFor(true, timeoutMillis))
      throw new Exception("MockResponseSender could not be closed");
  }

  public boolean isClosed() {
    return closed.isTrue();
  }

  public static class OutputStreamSender extends MockResponseSender {
    public OutputStreamSender(OutputStream out) {
      socket = new MockSocket(new PipedInputStream(), out);
    }

    public void doSending(Response response) throws Exception {
      response.readyToSend(this);
      while (!closed.isTrue())
        Thread.sleep(1000);
    }
  }
}
