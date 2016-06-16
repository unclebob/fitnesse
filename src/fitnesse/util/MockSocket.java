// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.util;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockSocket extends Socket {
  private static final Logger LOG = Logger.getLogger(MockSocket.class.getName());

  private final InputStream input;
  private final OutputStream output;
  private String host;
  private boolean closed;

  public MockSocket() throws IOException {
    PipedInputStream serverInput = new PipedInputStream();
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

  @Override
  public synchronized InputStream getInputStream() {
    return input;
  }

  @Override
  public synchronized OutputStream getOutputStream() {
    return output;
  }

  @Override
  public void close() {
    closed = true;
    try {
      input.close();
      output.close();
    }
    catch (IOException e) {
      LOG.log(Level.WARNING, "Unable to close IO", e);
    }
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    // Mock a socket address, to keep the logging happy.
    return new InetSocketAddress(host != null ? host : "internal", 123);
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    // Mock a socket address, to keep the logging happy.
    return new InetSocketAddress(host != null ? host : "internal", 123);
  }
}
