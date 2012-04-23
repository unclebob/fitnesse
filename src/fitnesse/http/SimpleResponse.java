// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;
import java.nio.ByteBuffer;

import util.StringUtil;

public class SimpleResponse extends Response {
  private byte[] content = new byte[0];

  public SimpleResponse() {
    super("html");
  }

  public SimpleResponse(int status) {
    super("html", status);
  }

  @Override
  public void sendTo(ResponseSender sender) {
    addStandardHeaders();
    try {
      sender.send(makeHttpHeaders().getBytes());
      sender.send(content);
    } finally {
      sender.close();
    }
  }

  public void setContent(String value) {
    content = getEncodedBytes(value);
  }

  public void setContent(byte[] value) {
    content = value;
  }

  @Override
  public String toString() {
    return String.format("status = %s,  contentType = %s, content = %s",
        getStatus(), getContentType(), getContent());
  }

  public String getContent() {
    return new String(content);
  }

  public byte[] getContentBytes() {
    return content;
  }

  @Override
  public int getContentSize() {
    return content.length;
  }

  @Override
  protected void addStandardHeaders() {
    super.addStandardHeaders();
    addHeader("Content-Length", String.valueOf(getContentSize()));
  }
}
