// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.wiki.PathParser;
import util.FileUtil;

import java.io.File;

public class FileSection {
  private static File fileSection;

  public FileSection(String type) throws Exception {
    if ("setup".equalsIgnoreCase(type)) {
      File dir = new File(FitnesseFixtureContext.context.getRootPagePath());
      dir.mkdir();
      fileSection = new File(dir, PathParser.FILES);
      fileSection.mkdir();
    } else {
      FileUtil.deleteFileSystemDirectory(FitnesseFixtureContext.context.getRootPagePath());
      fileSection = null;
    }
  }

  public static File getFileSection() {
    return fileSection;
  }

}
