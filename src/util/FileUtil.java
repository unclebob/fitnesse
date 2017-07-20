// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FileUtil {

  private static final Logger LOG = Logger.getLogger(FileUtil.class.getName());

  public static final String CHARENCODING = "UTF-8";

  public static File createFile(String path, String content) throws IOException {
    return createFile(path, new ByteArrayInputStream(content.getBytes()));
  }

  public static File createFile(String path, InputStream content) throws IOException {
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

  public static File createFile(File file, String content) throws IOException {
    return createFile(file, content.getBytes(CHARENCODING));
  }


  public static File createFile(File file, byte[] bytes) throws IOException {
    return createFile(file, new ByteArrayInputStream(bytes));
  }

  public static File createFile(File file, InputStream content) throws IOException {
    FileOutputStream fileOutput = null;
    try {
      fileOutput = new FileOutputStream(file);
      FileUtil.copyBytes(content, fileOutput);
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

  public static void deleteFileSystemDirectory(String dirPath) throws IOException {
    deleteFileSystemDirectory(new File(dirPath));
  }

  public static void deleteFileSystemDirectory(File current) throws IOException {
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

  public static void deleteFile(String filename) throws IOException {
    deleteFile(new File(filename));
  }

  public static void deleteFile(File file) throws IOException{
    if (!file.exists())
      return;
    if (!file.delete())
      throw new IOException("Could not delete '" + file.getAbsolutePath() + "'");
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
      close(stream);
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
      close(reader);
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

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        LOG.log(Level.INFO, "Unable to close " + closeable, e);
      }
    }
  }
}
