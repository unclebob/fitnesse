// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.File;

import util.FileUtil;

public class UploadedFile {
  private String name;
  private String type;
  private File file;

  public UploadedFile(String name, String type, File file) {
    this.name = name;
    this.type = type;
    this.file = file;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public File getFile() {
    return file;
  }

  public String toString() {
    try {
      return "name : " + getName() + "; type : " + getType() + "; content : " + FileUtil.getFileContent(file);
    }
    catch (Exception e) {
      e.printStackTrace();
      return e.toString();
    }
  }

  public boolean isUsable() {
    return (name != null && name.length() > 0);
  }

  public void delete() {
    file.delete();
  }
}
