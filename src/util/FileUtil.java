// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class FileUtil {

  public static File createFile(String path, String content) {
    return createFile(path, new ByteArrayInputStream(content.getBytes()));
  }

  public static File createFile(String path, InputStream content) {
    String names[] = path.split("/");
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
      return createFile(file, content.getBytes("UTF-8"));
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
    for (int i = 0; i < 10; i++) {
        if (file.delete()) {
            waitUntilFileDeleted(file);
            return;
        }
        waitFor(10);
    }
    throw new RuntimeException("Could not delete '" + file.getAbsoluteFile() + "'");
  }

  private static void waitUntilFileDeleted(File file) {
    int i = 10;
    while (file.exists()) {
      if (--i <= 0) {
        break;
      }
      waitFor(500);
    }
  }
    
    private static void waitFor(int milliseconds) {
        try {
          Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
        }
    }

  public static String getFileContent(String path) throws IOException {
    File input = new File(path);
    return getFileContent(input);
  }

  public static String getFileContent(File input) throws IOException {
    return new String(getFileBytes(input), "UTF-8");
  }

  public static byte[] getFileBytes(File input) throws IOException {
    long size = input.length();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(input);
      byte[] bytes = new StreamReader(stream).readBytes((int) size);
      return bytes;
    } finally {
      if (stream != null)
        stream.close();
    }
  }

  public static LinkedList<String> getFileLines(String filename) throws IOException {
    return getFileLines(new File(filename));
  }

  public static LinkedList<String> getFileLines(File file) throws IOException {
    LinkedList<String> lines = new LinkedList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    while ((line = reader.readLine()) != null)
      lines.add(line);

    reader.close();
    return lines;
  }

  public static void writeLinesToFile(File file, List<?> lines) throws FileNotFoundException {
    PrintStream output = new PrintStream(new FileOutputStream(file));
    for (Iterator<?> iterator = lines.iterator(); iterator.hasNext();) {
      String line = (String) iterator.next();
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
    Scanner s = new Scanner(input, "UTF-8");
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
    SortedSet<File> dirSet = new TreeSet<File>();
    SortedSet<File> fileSet = new TreeSet<File>();
    File[] files = dir.listFiles();
    if (files == null)
      return new File[0];
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory())
        dirSet.add(files[i]);
      else
        fileSet.add(files[i]);
    }
    List<File> fileList = new LinkedList<File>();
    fileList.addAll(dirSet);
    fileList.addAll(fileSet);
    return fileList.toArray(new File[fileList.size()]);
  }

  public static String buildPath(String[] parts) {
    return StringUtil.join(Arrays.asList(parts), System.getProperty("file.separator"));
  }

  public static List<String> breakFilenameIntoParts(String fileName) {
    List<String> parts = new ArrayList<String>(Arrays.asList(fileName.split("/")));
    return parts;
  }

  public static String getPathOfFile(String fileName) {
    List<String> parts = breakFilenameIntoParts(fileName);
    parts.remove(parts.size()-1);
    return buildPath(parts.toArray(new String[parts.size()]));
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

}
