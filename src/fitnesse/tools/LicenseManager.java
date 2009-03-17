// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.tools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import util.FileUtil;

public abstract class LicenseManager {
  public static void main(String[] args) throws Exception {
    if ("-r".equals(args[0])) {
      File directory = new File(args[1]);
      new LicenseRemover().doDirectory(directory);
    } else {
      File directory = new File(args[1]);
      new LicenseAdder(args[0]).doDirectory(directory);
    }
  }

  protected void doDirectory(File directory) throws Exception {
    File files[] = FileUtil.getDirectoryListing(directory);
    for (File file : files) {
      if (file.isDirectory())
        doDirectory(file);
      else if (file.getName().endsWith(".java"))
        doFile(file);
    }
  }

  protected abstract void doFile(File file) throws Exception;
}

class LicenseAdder extends LicenseManager {
  private LinkedList<String> license;

  public LicenseAdder(String licenseFileName) throws Exception {
    super();
    license = FileUtil.getFileLines(licenseFileName);
  }

  protected void doFile(File file) throws Exception {
    System.out.println("Adding license to " + file.getPath());
    List<String> lines = FileUtil.getFileLines(file);
    lines.addAll(0, license);
    FileUtil.writeLinesToFile(file, lines);
  }
}

class LicenseRemover extends LicenseManager {
  protected void doFile(File file) throws Exception {
    System.out.println("Removing license from " + file.getPath());
    LinkedList<String> lines = FileUtil.getFileLines(file);
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).startsWith("// Copyright (C) ")) {
        lines.remove(i);
        lines.remove(i);
        FileUtil.writeLinesToFile(file, lines);
        return;
      }
    }
  }
}
