// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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
  public void send(byte[] bytes) throws IOException {
    output.write(bytes);
  }

  @Override
  public void close() {
    closed = true;
  }

  public String sentData() throws UnsupportedEncodingException {
    return ((ByteArrayOutputStream) output).toString(FileUtil.CHARENCODING);
  }

  public void doSending(Response response) throws IOException {
    response.sendTo(this);
    assert closed;
  }

  public boolean isClosed() {
    return closed;
  }
  }
