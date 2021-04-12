// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {

  private static final Logger LOG = Logger.getLogger(FileUtil.class.getName());

  public static final String CHARENCODING = StandardCharsets.UTF_8.name();

  public static File createFile(String path, String content) throws IOException {
    return createFile(path, new ByteArrayInputStream(content.getBytes()));
  }

  public static File createFile(String path, InputStream content) throws IOException {
    File file = new File(path);
    if (path.contains("/")) {
      File parent = file.getParentFile();
      parent.mkdirs();
    }
    return createFile(file, content);
  }

  public static File createFile(File file, String content) throws IOException {
    return createFile(file, content.getBytes(StandardCharsets.UTF_8));
  }


  public static File createFile(File file, byte[] bytes) throws IOException {
    return createFile(file, new ByteArrayInputStream(bytes));
  }

  public static File createFile(File file, InputStream content) throws IOException {
    Files.copy(content, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return file;
  }

  public static boolean makeDir(String path) {
    return new File(path).mkdir();
  }

  public static void deleteFileSystemDirectory(String dirPath) throws IOException {
    deleteFileSystemDirectory(new File(dirPath));
  }

  public static void deleteFileSystemDirectory(File current) throws IOException {
    for (File child : listFiles(current)) {
      deleteFileSystemDirectory(child);
    }
    deleteFile(current);
  }

  public static void deleteFile(String filename) throws IOException {
    deleteFile(new File(filename));
  }

  public static void deleteFile(File file) throws IOException {
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
    return new String(getFileBytes(input), StandardCharsets.UTF_8);
  }

  public static byte[] getFileBytes(File input) throws IOException {
    return Files.readAllBytes(input.toPath());
  }

  public static List<String> getFileLines(File file) throws IOException {
    return Files.readAllLines(file.toPath(), Charset.defaultCharset());
  }

  public static void writeLinesToFile(File file, List<String> lines) throws IOException {
    Files.write(file.toPath(), lines, Charset.defaultCharset());
  }

  public static void copyBytes(InputStream input, OutputStream output) throws IOException {
    StreamReader reader = new StreamReader(input);
    while (!reader.isEof())
      output.write(reader.readBytes(1000));
  }

  public static String toString(InputStream input) {
    try (Scanner s = new Scanner(input, CHARENCODING)) {
      s.useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
    }
  }

  public static File createDir(String path) {
    makeDir(path);
    return new File(path);
  }

  public static File[] getDirectoryListing(File dir) {
    File[] files = listFiles(dir);
    if (files.length > 0) {
      SortedSet<File> dirSet = new TreeSet<>();
      SortedSet<File> fileSet = new TreeSet<>();
      for (File file : files) {
        if (file.isDirectory())
          dirSet.add(file);
        else
          fileSet.add(file);
      }
      List<File> fileList = new ArrayList<>(files.length);
      fileList.addAll(dirSet);
      fileList.addAll(fileSet);
      files = fileList.toArray(new File[0]);
    }
    return files;
  }

  public static File[] listFiles(File dir) {
    return listFiles(dir, p -> true);
  }

  public static File[] listFiles(File dir, DirectoryStream.Filter<Path> visitPredicate) {
    if (!dir.isDirectory()) {
      return new File[0];
    }
    List<File> fileList = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), visitPredicate)) {
      for (Path path : stream) {
        fileList.add(path.toFile());
      }
    } catch (IOException e) {
      // not expected, ignore
    }
    return fileList.toArray(new File[0]);
  }

  public static boolean isEmpty(File directory) throws IOException {
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory.toPath())) {
      return !dirStream.iterator().hasNext();
    }
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
