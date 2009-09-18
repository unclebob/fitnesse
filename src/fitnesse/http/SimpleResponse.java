// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.nio.ByteBuffer;

public class SimpleResponse extends Response {
  private byte[] content = new byte[0];

  public SimpleResponse() {
    super("html");
  }

  public SimpleResponse(int status) {
    super("html", status);
  }

  @Override
  public void readyToSend(ResponseSender sender) throws Exception {
    byte[] bytes = getBytes();
    sender.send(bytes);
    sender.close();
  }

  public void setContent(String value) throws Exception {
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

  public String getText() {
    return new String(getBytes());
  }

  public byte[] getBytes() {
    addStandardHeaders();
    byte[] headerBytes = makeHttpHeaders().getBytes();
    ByteBuffer bytes = ByteBuffer.allocate(headerBytes.length
        + getContentSize());
    bytes.put(headerBytes).put(content);
    return bytes.array();
  }

  @Override
  public int getContentSize() {
    return content.length;
  }

  @Override
  protected void addSpecificHeaders() {
    addHeader("Content-Length", String.valueOf(getContentSize()));
  }
}
