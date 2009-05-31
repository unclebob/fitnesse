// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MockSocket extends Socket {
  InputStream input;
  OutputStream output;
  private String host;
  private boolean closed;

  public MockSocket() throws Exception {
    PipedInputStream serverInput = new PipedInputStream();
    @SuppressWarnings("unused")
    PipedOutputStream clientOutput = new PipedOutputStream(serverInput);
    PipedInputStream clientInput = new PipedInputStream();
    PipedOutputStream serverOutput = new PipedOutputStream(clientInput);
    input = serverInput;
    output = serverOutput;
  }

  public MockSocket(String input) {
    this.input = new ByteArrayInputStream(input.getBytes());
    output = new ByteArrayOutputStream();
  }

  public MockSocket(InputStream input, OutputStream output) {
    this.input = input;
    this.output = output;
  }

  public InputStream getInputStream() {
    return input;
  }

  public OutputStream getOutputStream() {
    return output;
  }

  public void close() {
    closed = true;
    try {
      input.close();
      output.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public String getOutput() throws Exception {
    if (output instanceof ByteArrayOutputStream)
      return ((ByteArrayOutputStream) output).toString("UTF-8");
    else
      return "";
  }

  public void setHost(String host) {
    this.host = host;
  }

  public SocketAddress getRemoteSocketAddress() {
    return new InetSocketAddress(host, 123);
  }
}
