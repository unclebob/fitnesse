// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.Socket;

import fitnesse.util.MockSocket;

public class MockResponseSender implements ResponseSender {
  public MockSocket socket;
  protected boolean closed;

  public MockResponseSender() {
    socket = new MockSocket("Mock");
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
    closed = true;
  }

  @Override
  public Socket getSocket() {
    return socket;
  }

  public String sentData() {
    return socket.getOutput();
  }

  public void doSending(Response response) throws IOException {
    response.sendTo(this);
    assert closed;
  }

  public boolean isClosed() {
    return closed;
  }

  public static class OutputStreamSender extends MockResponseSender {
    public OutputStreamSender(OutputStream out) {
      socket = new MockSocket(new PipedInputStream(), out);
    }

    public void doSending(Response response) throws IOException {
      response.sendTo(this);
      assert closed;
    }
  }
}
