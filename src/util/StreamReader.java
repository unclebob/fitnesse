// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamReader {
  private InputStream input;
  private State state;

  ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
  OutputStream output;

  private int readGoal;
  private int readStatus;

  private boolean eof = false;

  private byte[] boundary;
  private int boundaryLength;
  private int matchingBoundaryIndex;
  private byte[] matchedBoundaryBytes;

  private long bytesConsumed;

  public StreamReader(InputStream input) {
    this.input = input;
  }

  public void close() throws Exception {
    input.close();
  }

  public String readLine() throws Exception {
    return bytesToString(readLineBytes());
  }

  public byte[] readLineBytes() throws Exception {
    state = READLINE_STATE;
    return preformRead();
  }

  public String read(int count) throws Exception {
    return bytesToString(readBytes(count));
  }

  public byte[] readBytes(int count) throws Exception {
    readGoal = count;
    readStatus = 0;
    state = READCOUNT_STATE;
    return preformRead();
  }

  public void copyBytes(int count, OutputStream output) throws Exception {
    readGoal = count;
    state = READCOUNT_STATE;
    performCopy(output);
  }

  public String readUpTo(String boundary) throws Exception {
    return bytesToString(readBytesUpTo(boundary));
  }

  public byte[] readBytesUpTo(String boundary) throws Exception {
    prepareForReadUpTo(boundary);
    return preformRead();
  }

  private void prepareForReadUpTo(String boundary) {
    this.boundary = boundary.getBytes();
    boundaryLength = this.boundary.length;
    matchedBoundaryBytes = new byte[boundaryLength];
    matchingBoundaryIndex = 0;
    state = READUPTO_STATE;
  }

  public void copyBytesUpTo(String boundary, OutputStream outputStream) throws Exception {
    prepareForReadUpTo(boundary);
    performCopy(outputStream);
  }

  public int byteCount() {
    return byteBuffer.size();
  }

  public byte[] getBufferedBytes() {
    return byteBuffer.toByteArray();
  }

  private byte[] preformRead() throws Exception {
    setReadMode();
    clearBuffer();
    readUntilFinished();
    return getBufferedBytes();
  }

  private void performCopy(OutputStream output) throws Exception {
    setCopyMode(output);
    readUntilFinished();
  }

  private void readUntilFinished() throws Exception {
    while (!state.finished())
      state.read(input);
  }

  private void clearBuffer() {
    byteBuffer.reset();
  }

  private void setCopyMode(OutputStream output) {
    this.output = output;
  }

  private void setReadMode() {
    output = byteBuffer;
  }

  private String bytesToString(byte[] bytes) throws Exception {
    return new String(bytes, "UTF-8");
  }

  private void changeState(State state) {
    this.state = state;
  }

  public boolean isEof() {
    return eof;
  }

  public long numberOfBytesConsumed() {
    return bytesConsumed;
  }

  public void resetNumberOfBytesConsumed() {
    bytesConsumed = 0;
  }

  private static abstract class State {
    public void read(InputStream input) throws Exception {
    }

    public boolean finished() {
      return false;
    }
  }

  private final State READLINE_STATE = new State() {
    public void read(InputStream input) throws Exception {
      int b = input.read();
      if (b == -1) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed++;
        if (b == '\n')
          changeState(FINAL_STATE);
        else if (b != '\r')
          output.write((byte) b);
      }
    }
  };

  private final State READCOUNT_STATE = new State() {
    public void read(InputStream input) throws Exception {
      byte[] bytes = new byte[readGoal - readStatus];
      int bytesRead = input.read(bytes);

      if (bytesRead < 0) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed += bytesRead;
        readStatus += bytesRead;
        output.write(bytes, 0, bytesRead);
      }
    }

    public boolean finished() {
      return readStatus >= readGoal;
    }
  };

  private final State READUPTO_STATE = new State() {
    public void read(InputStream input) throws Exception {
      int b = input.read();
      if (b == -1) {
        changeState(FINAL_STATE);
        eof = true;
      } else {
        bytesConsumed++;
        if (b == boundary[matchingBoundaryIndex]) {
          matchedBoundaryBytes[matchingBoundaryIndex++] = (byte) b;
          if (matchingBoundaryIndex >= boundaryLength)
            changeState(FINAL_STATE);
        } else if (matchingBoundaryIndex == 0)
          output.write((byte) b);
        else {
          output.write(matchedBoundaryBytes, 0, matchingBoundaryIndex);
          matchingBoundaryIndex = 0;
          if (b == boundary[matchingBoundaryIndex])
            matchedBoundaryBytes[matchingBoundaryIndex++] = (byte) b;
          else
            output.write((byte) b);
        }
      }
    }
  };

  private final State FINAL_STATE = new State() {
    public boolean finished() {
      return true;
    }
  };
}
