// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileUpdate implements Update {

  protected final File destination;
  protected final String source;
  protected final String filename;

  public FileUpdate(String source, File destination) {
    this.destination = destination;
    this.source = source;

    filename = new File(source).getName();
  }

  @Override
  public void doUpdate() throws IOException {
    makeSureDirectoriesExist();
    copyResource();
  }

  private void makeSureDirectoriesExist() {
    destination.mkdirs();
  }

  private void copyResource() throws IOException {
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
      throw new FileNotFoundException("Could not load resource: " + source);
  }

  protected URL getResource(String resource) {
    return getClass().getClassLoader().getResource(resource);
  }

  @Override
  public String getMessage() {
    return ".";
  }

  protected File destinationFile() {
    return new File(destination, filename);
  }

  @Override
  public String getName() {
    return "FileUpdate(" + filename + ")";
  }

  @Override
  public boolean shouldBeApplied() throws IOException {
    return !destinationFile().exists();
  }
}
