package fitnesse.tools;

import fitnesse.util.FileUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
    List<String> lines = FileUtil.getFileLines(file);
    lines.addAll(0, license);
    FileUtil.writeLinesToFile(file, lines);
  }
}

class LicenseRemover extends LicenseManager {
  protected void doFile(File file) throws Exception {
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
