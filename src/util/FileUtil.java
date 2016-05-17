// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class FileUtil {

  public static final String CHARENCODING = "UTF-8";

  public static File createFile(String path, String content) {
    return createFile(path, new ByteArrayInputStream(content.getBytes()));
  }

  public static File createFile(String path, InputStream content) {
    String[] names = path.replace("/", File.separator).split(Pattern.quote(File.separator));
    if (names.length == 1)
      return createFile(new File(path), content);
    else {
      File parent = null;
      for (int i = 0; i < names.length - 1; i++) {
        parent = parent == null ? new File(names[i]) : new File(parent, names[i]);
        if (!parent.exists())
          parent.mkdir();
      }
      File fileToCreate = new File(parent, names[names.length - 1]);
      return createFile(fileToCreate, content);
    }
  }

  public static File createFile(File file, String content) {
    try {
      return createFile(file, content.getBytes(CHARENCODING));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }


  public static File createFile(File file, byte[] bytes) {
    return createFile(file, new ByteArrayInputStream(bytes));
  }

  public static File createFile(File file, InputStream content) {
    FileOutputStream fileOutput = null;
    try {
      fileOutput = new FileOutputStream(file);
      FileUtil.copyBytes(content, fileOutput);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (fileOutput != null)
        try {
          fileOutput.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
    return file;
  }

  public static boolean makeDir(String path) {
    return new File(path).mkdir();
  }

  public static void deleteFileSystemDirectory(String dirPath) {
    deleteFileSystemDirectory(new File(dirPath));
  }

  public static void deleteFileSystemDirectory(File current) {
    File[] files = current.listFiles();

    for (int i = 0; files != null && i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory())
        deleteFileSystemDirectory(file);
      else
        deleteFile(file);
    }
    deleteFile(current);
  }

  public static void deleteFile(String filename) {
    deleteFile(new File(filename));
  }

  public static void deleteFile(File file) {
    if (!file.exists())
      return;
    if (!file.delete())
      throw new RuntimeException("Could not delete '" + file.getAbsolutePath() + "'");
  }

  public static String getFileContent(String path) throws IOException {
    File input = new File(path);
    return getFileContent(input);
  }

  public static String getFileContent(File input) throws IOException {
    return new String(getFileBytes(input), CHARENCODING);
  }

  public static byte[] getFileBytes(File input) throws IOException {
    long size = input.length();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(input);
      return new StreamReader(stream).readBytes((int) size);
    } finally {
      if (stream != null)
        stream.close();
    }
  }

  public static LinkedList<String> getFileLines(File file) throws IOException {
    LinkedList<String> lines = new LinkedList<>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    try {
      while ((line = reader.readLine()) != null)
        lines.add(line);
    } finally {
      reader.close();
    }
    return lines;
  }

  public static void writeLinesToFile(File file, List<String> lines) throws FileNotFoundException {
    PrintStream output = new PrintStream(new FileOutputStream(file));
    for (String line : lines) {
      output.println(line);
    }
    output.close();
  }

  public static void copyBytes(InputStream input, OutputStream output) throws IOException {
    StreamReader reader = new StreamReader(input);
    while (!reader.isEof())
      output.write(reader.readBytes(1000));
  }

  public static String toString(InputStream input) throws IOException {
    String result = "";
    Scanner s = new Scanner(input, CHARENCODING);
    s.useDelimiter("\\A");
    result = s.hasNext() ? s.next() : "";
    s.close();
    return result;
   }

  public static File createDir(String path) {
    makeDir(path);
    return new File(path);
  }

  public static File[] getDirectoryListing(File dir) {
    SortedSet<File> dirSet = new TreeSet<>();
    SortedSet<File> fileSet = new TreeSet<>();
    File[] files = dir.listFiles();
    if (files == null)
      return new File[0];
    for (File file : files) {
      if (file.isDirectory())
        dirSet.add(file);
      else
        fileSet.add(file);
    }
    List<File> fileList = new LinkedList<>();
    fileList.addAll(dirSet);
    fileList.addAll(fileSet);
    return fileList.toArray(new File[fileList.size()]);
  }

  public static void close(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        throw new RuntimeException("Unable to close writer", e);
      }
    }
  }

  public static void close(OutputStream output) {
    if (output != null) {
      try {
        output.close();
      } catch (IOException e) {
        throw new RuntimeException("Unable to close outputstream", e);
      }
    }
  }

  public static void close(InputStream input) {
    if (input != null) {
      try {
        input.close();
      } catch (IOException e) {
        throw new RuntimeException("Unable to close inputstream", e);
      }
    }
  }

  public static void close(StreamReader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        throw new RuntimeException("Unable to close stream reader", e);
      }
    }
  }
}
