// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fit;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import util.FileUtil;
import util.StreamReader;

public class FitProtocol {
  public static final DecimalFormat format = new DecimalFormat("0000000000");

  public static void writeData(String data, OutputStream output) throws IOException {
    byte[] bytes = data.getBytes(FileUtil.CHARENCODING);
    writeData(bytes, output);
  }

  public static void writeData(byte[] bytes, OutputStream output) throws IOException {
    int length = bytes.length;
    writeSize(length, output);
    output.write(bytes);
    output.flush();
  }

  public static void writeSize(int length, OutputStream output) throws IOException {
    String formattedLength = format.format(length);
    byte[] lengthBytes = formattedLength.getBytes();
    output.write(lengthBytes);
    output.flush();
  }

  public static void writeCounts(Counts count, OutputStream output) throws IOException {
    writeSize(0, output);
    writeSize(count.right, output);
    writeSize(count.wrong, output);
    writeSize(count.ignores, output);
    writeSize(count.exceptions, output);
  }

  public static int readSize(StreamReader reader) throws IOException {
    String sizeString = reader.read(10);
    if (sizeString.length() < 10)
      throw new IOException("A size value could not be read. Fragment=|" + sizeString + "|");
    else
      return Integer.valueOf(sizeString).intValue();
  }

  public static String readDocument(StreamReader reader, int size) throws IOException {
    return reader.read(size);
  }

  public static Counts readCounts(StreamReader reader) throws IOException {
    Counts counts = new Counts();
    counts.right = readSize(reader);
    counts.wrong = readSize(reader);
    counts.ignores = readSize(reader);
    counts.exceptions = readSize(reader);
    return counts;
  }
}
