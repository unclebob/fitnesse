// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class StreamReaderTest extends RegexTestCase {
  private PipedOutputStream output;
  private StreamReader reader;
  private String readResult;
  private byte[] byteResult;
  private Thread thread;
  @SuppressWarnings("unused")
  private Exception exception;

  public void setUp() throws Exception {
    output = new PipedOutputStream();
    reader = new StreamReader(new PipedInputStream(output));
  }

  public void tearDown() throws Exception {
    output.close();
    reader.close();
  }

  private void writeToPipe(String value) throws Exception {
    byte[] bytes = value.getBytes();
    output.write(bytes);
  }

  public void testReadLine() throws Exception {
    startReading(new ReadLine());
    writeToPipe("a line\r\n");
    finishReading();
    assertEquals("a line", readResult);
  }

  public void testReadLineBytes() throws Exception {
    startReading(new ReadLineBytes());
    writeToPipe("a line\r\n");
    finishReading();
    assertEquals("a line", new String(byteResult));
  }

  public void testBufferCanGrow() throws Exception {
    startReading(new ReadLine());
    for (int i = 0; i < 1001; i++)
      writeToPipe(i + ",");
    writeToPipe("\r\n");

    finishReading();
    assertHasRegexp("1000", readResult);
  }

  public void testReadNumberOfBytesAsString() throws Exception {
    startReading(new ReadCount(100));
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 100; i++) {
      buffer.append("*");
      writeToPipe("*");
    }
    finishReading();

    assertEquals(buffer.toString(), readResult);
  }

  public void testReadNumberOfBytes() throws Exception {
    startReading(new ReadCountBytes(100));
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 100; i++) {
      buffer.append("*");
      writeToPipe("*");
    }
    finishReading();

    assertEquals(buffer.toString(), new String(byteResult));
  }

  public void testReadNumberOfBytesWithClosedInput() throws Exception {
    startReading(new ReadCountBytes(100));

    for (int i = 0; i < 50; i++)
      writeToPipe("*");
    output.close();
    finishReading();

    assertEquals("bytes consumed", 50, reader.numberOfBytesConsumed());
    assertEquals("bytes returned", 50, byteResult.length);
  }

  public void testReadingZeroBytes() throws Exception {
    startReading(new ReadCount(0));
    finishReading();
    assertEquals("", readResult);
  }

  public void testReadUpTo() throws Exception {
    checkReadUoTo("--boundary", "some bytes--boundary", "some bytes");
  }

  public void testReadUpToNonEnd() throws Exception {
    checkReadUoTo("--bound", "some bytes--boundary", "some bytes");
  }

  public void testReadBytesUpTo() throws Exception {
    startReading(new ReadUpToBytes("--boundary"));
    writeToPipe("some bytes--boundary");
    finishReading();

    assertEquals("some bytes", new String(byteResult));
  }

  public void testReadUpTo2() throws Exception {
    checkReadUoTo("--bob", "----bob\r\n", "--");
  }

  public void testReadUpTo3() throws Exception {
    checkReadUoTo("12345", "112123123412345", "1121231234");
  }

  private void checkReadUoTo(String boundary, String input, String expected) throws Exception {
    startReading(new ReadUpTo(boundary));
    writeToPipe(input);
    finishReading();

    assertEquals(expected, readResult);
  }

  public void testCopyBytesUpTo() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    writeToPipe("some bytes--boundary");
    reader.copyBytesUpTo("--boundary", outputStream);
    assertEquals("some bytes", outputStream.toString());
  }

  public void testEofReadCount() throws Exception {
    writeToPipe("abcdefghijklmnopqrstuvwxyz");
    output.close();
    assertFalse(reader.isEof());
    reader.read(10);
    assertFalse(reader.isEof());
    reader.read(16);
    assertFalse(reader.isEof());
    reader.read(1);
    assertTrue(reader.isEof());
  }

  public void testEofReadLine() throws Exception {
    writeToPipe("one line\ntwo lines\nthree lines");
    output.close();
    assertFalse(reader.isEof());
    reader.readLine();
    assertFalse(reader.isEof());
    reader.readLine();
    assertFalse(reader.isEof());
    reader.readLine();
    assertTrue(reader.isEof());
  }

  public void testEofReadUpTo() throws Exception {
    writeToPipe("mark one, mark two, the end");
    output.close();
    assertFalse(reader.isEof());
    reader.readUpTo("one");
    assertFalse(reader.isEof());
    reader.readUpTo("two");
    assertFalse(reader.isEof());
    reader.readUpTo("three");
    assertTrue(reader.isEof());
  }

  public void testBytesConsumed() throws Exception {
    writeToPipe("One line\r\n12345abc-boundary");
    assertEquals(0, reader.numberOfBytesConsumed());

    reader.readLine();
    assertEquals(10, reader.numberOfBytesConsumed());

    reader.read(5);
    assertEquals(15, reader.numberOfBytesConsumed());

    reader.readUpTo("-boundary");
    assertEquals(27, reader.numberOfBytesConsumed());
  }

  public void testEarlyClosingStream() throws Exception {
    startReading(new ReadCount(10));
    output.close();
    finishReading();
    assertEquals("", readResult);
  }

  private void startReading(ReadThread thread) {
    this.thread = thread;
    this.thread.start();
  }

  private void finishReading() throws Exception {
    thread.join();
  }

  abstract class ReadThread extends Thread {
    public void run() {
      try {
        doRead();
      }
      catch (Exception e) {
        exception = e;
      }
    }

    public abstract void doRead() throws Exception;
  }

  class ReadLine extends ReadThread {
    public void doRead() throws Exception {
      readResult = reader.readLine();
    }
  }

  class ReadCount extends ReadThread {
    private int amount;

    public ReadCount(int amount) {
      this.amount = amount;
    }

    public void doRead() throws Exception {
      readResult = reader.read(amount);
    }
  }

  class ReadUpTo extends ReadThread {
    private String boundary;

    public ReadUpTo(String b) {
      boundary = b;
    }

    public void doRead() throws Exception {
      readResult = reader.readUpTo(boundary);
    }
  }

  class ReadLineBytes extends ReadThread {
    public void doRead() throws Exception {
      byteResult = reader.readLineBytes();
    }
  }

  class ReadCountBytes extends ReadThread {
    private int amount;

    public ReadCountBytes(int amount) {
      this.amount = amount;
    }

    public void doRead() throws Exception {
      byteResult = reader.readBytes(amount);
    }
  }

  class ReadUpToBytes extends ReadThread {
    private String boundary;

    public ReadUpToBytes(String b) {
      boundary = b;
    }

    public void doRead() throws Exception {
      byteResult = reader.readBytesUpTo(boundary);
    }
  }

}
