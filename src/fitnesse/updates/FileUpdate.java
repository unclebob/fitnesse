// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileUpdate implements Update {
  private static final String slash = "/";

  protected String destination;
  protected String source;
  protected File destinationDir;
  protected String rootDir;
  protected String filename;

  public FileUpdate(UpdaterImplementation updater, String source, String destination) throws Exception {
    this.destination = destination;
    this.source = source;
    rootDir = updater.context.rootPagePath;
    destinationDir = new File(new File(rootDir), destination);

    filename = new File(source).getName();
  }

  public void doUpdate() throws Exception {
    makeSureDirectoriesExist();
    copyResource();
  }

  private void makeSureDirectoriesExist() {
    String[] subDirectories = destination.split(slash);
    String currentDirPath = rootDir;

    for (int i = 0; i < subDirectories.length; i++) {
      String subDirectory = subDirectories[i];
      currentDirPath = currentDirPath + slash + subDirectory;
      File directory = new File(currentDirPath);
      directory.mkdir();
    }
  }

  private void copyResource() throws Exception {
    URL url = getResource(source);
    if (url != null) {
      InputStream input = null;
      OutputStream output = null;
      try {
        input = url.openStream();
        output = new FileOutputStream(destinationFile());

        int b;
        while ((b = input.read()) != -1)
          output.write(b);
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
      }
    } else
      throw new Exception("Could not load resource: " + source);
  }

  protected URL getResource(String resource) {
    return ClassLoader.getSystemResource(resource);
  }

  public String getMessage() {
    return "Installing file: " + destinationFile();
  }

  protected File destinationFile() {
    return new File(destinationDir, filename);
  }

  public String getName() {
    return "FileUpdate(" + filename + ")";
  }

  public boolean shouldBeApplied() throws Exception {
    return !destinationFile().exists();
  }
}
