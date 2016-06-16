// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class ChunkedResponse extends Response implements Closeable {
  private ResponseSender sender;
  private int bytesSent = 0;
  private boolean dontChunk = false;
  private ChunkedDataProvider chunckedDataProvider;

  public ChunkedResponse(String format, ChunkedDataProvider chunckedDataProvider) {
    super(format);
    this.chunckedDataProvider = chunckedDataProvider;
    if (isTextFormat())
      dontChunk = true;
  }

  @Override
  public void sendTo(ResponseSender sender) throws IOException {
    this.sender = sender;
    sender.send(makeHttpHeaders().getBytes());
    chunckedDataProvider.startSending();
  }

  @Override
  protected void addContentHeaders() {
    super.addContentHeaders();
    if (!dontChunk)
      addHeader("Transfer-Encoding", "chunked");
  }

  public static String asHex(int value) {
    return Integer.toHexString(value);
  }

  public void add(String text) throws IOException {
    if (text != null)
      add(getEncodedBytes(text));
  }

  public void add(byte[] bytes) throws IOException {
    if (bytes == null || bytes.length == 0)
      return;
    if (dontChunk) {
      sender.send(bytes);
    } else {
      String sizeLine = asHex(bytes.length) + CRLF;
      ByteBuffer chunk = ByteBuffer.allocate(sizeLine.length() + bytes.length + 2);
      chunk.put(sizeLine.getBytes()).put(bytes).put(CRLF.getBytes());
      sender.send(chunk.array());
    }
    bytesSent += bytes.length;
  }

  public void addTrailingHeader(String key, String value) throws IOException {
    if (!dontChunk) {
      String header = key + ": " + value + CRLF;
      sender.send(header.getBytes());
    }
  }

  public void closeChunks() throws IOException {
    if (!dontChunk) {
      sender.send(("0" + CRLF).getBytes());
    }
  }

  public void closeTrailer() throws IOException {
    if (!dontChunk) {
      sender.send(CRLF.getBytes());
    }
  }

  @Override
  public void close() throws IOException {
    closeChunks();
    closeTrailer();
    sender.close();
  }

  @Override
  public int getContentSize() {
    return bytesSent;
  }

  public void turnOffChunking() {
    dontChunk = true;
  }

  public boolean isChunkingTurnedOff() {
    return dontChunk;
  }

  public Writer getWriter() {
    return new  Writer() {

      @Override
      public void close() throws IOException {
        //sender.close();
      }

      @Override
      public void flush() throws IOException {
        // sender.flush(); -- flush is done on write
      }

      @Override
      public void write(String str) throws IOException {
        add(str);
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        write(new String(cbuf, off, len));
      }
    };
  }
}
