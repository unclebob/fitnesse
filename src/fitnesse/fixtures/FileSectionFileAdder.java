// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.File;

import fit.ColumnFixture;

public class FileSectionFileAdder extends ColumnFixture {
  public String path;
  public String type;

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
}
