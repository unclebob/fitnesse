// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.File;
import java.io.FileWriter;

public class FileSectionFileAdder {
  private String path;
  private String type;
  private String content = "";

  public boolean valid() throws Exception {
    File file;
    if ("dir".equals(type)) {
      file = new File(FileSection.getFileSection(), path);
      file.mkdir();
    } else {
      file = new File(FileSection.getFileSection(), path);
      try (FileWriter fw = new FileWriter(file)) {
        fw.append(content);
      }
      content = "";
    }
    return file.exists();
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
