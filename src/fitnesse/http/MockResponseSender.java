// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import fitnesse.util.MockSocket;
import util.FileUtil;

public class MockResponseSender implements ResponseSender {
  private final OutputStream output;
  protected boolean closed;

  public MockResponseSender() {
    this(new ByteArrayOutputStream());
  }

  public MockResponseSender(OutputStream output) {
    this.output = output;
  }

  @Override
  public void send(byte[] bytes) {
    try {
      output.write(bytes);
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
    return new MockSocket(new PipedInputStream(), output);
  }

  public String sentData() {
    try {
      return ((ByteArrayOutputStream) output).toString(FileUtil.CHARENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unable to decode output stream", e);
    }
  }

  public void doSending(Response response) throws IOException {
    response.sendTo(this);
    assert closed;
  }

  public boolean isClosed() {
    return closed;
  }
  }
