// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.File;

public class FileSectionFileAdder {
  private String path;
  private String type;

  public boolean valid() throws Exception {
    File file = null;
    if ("dir".equals(type)) {
      file = new File(FileSection.getFileSection().getPath() + "/" + path);
      file.mkdir();
    } else {
      file = new File(FileSection.getFileSection().getPath() + "/" + path);
      file.createNewFile();
    }
    return file.exists();
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setType(String type) {
    this.type = type;
  }
}
