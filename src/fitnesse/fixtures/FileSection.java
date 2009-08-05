// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import util.FileUtil;

import java.io.File;

public class FileSection {
  private static File fileSection;

  public FileSection(String type) throws Exception {
    if ("setup".equals(type.toLowerCase())) {
      new File(FitnesseFixtureContext.baseDir).mkdir();
      File dir = new File(FitnesseFixtureContext.baseDir + "/" + FitnesseFixtureContext.root.getName());
      dir.mkdir();
      fileSection = new File(dir, "files");
      fileSection.mkdir();
    } else {
      FileUtil.deleteFileSystemDirectory(FitnesseFixtureContext.baseDir);
      fileSection = null;
    }
  }

  public static File getFileSection() {
    return fileSection;
  }

}
