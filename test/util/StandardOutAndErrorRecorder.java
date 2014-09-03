// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

public class StandardOutAndErrorRecorder {
  private PrintStream originalErrStream;
  private PrintStream originalOutStream;
  private ByteArrayOutputStream recordedErrStream;
  private ByteArrayOutputStream recordedOutStream;

  public StandardOutAndErrorRecorder() {
    beginRecording();
  }

  private void beginRecording() {
    recordOriginalStreams();
    redirectStreams();
  }

  private void redirectStreams() {
    recordedErrStream = new ByteArrayOutputStream();
    System.setErr(new PrintStream(recordedErrStream));

    recordedOutStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(recordedOutStream));
  }

  private void recordOriginalStreams() {
    originalErrStream = System.err;
    originalOutStream = System.out;
  }

  public void stopRecording(boolean report) {
    closeAllStreams();
    replaceOriginalStreams();
    reportStreams(report);
    nullAllStreams();
  }

  private void reportStreams(boolean report) {
    if (report) {
      System.out.print(recordedOutStream.toString());
      System.err.print(recordedErrStream.toString());
    }
  }

  private void replaceOriginalStreams() {
    System.setErr(originalErrStream);
    System.setOut(originalOutStream);
  }

  private void closeAllStreams() {
    closeStream(recordedOutStream);
    closeStream(recordedErrStream);
    closeStream(System.err);
    closeStream(System.out);
  }

  private void nullAllStreams() {
    originalErrStream = null;
    originalOutStream = null;
    recordedErrStream = null;
    recordedOutStream = null;
  }

  private void closeStream(Closeable stream) {
    try {
      recordedOutStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
