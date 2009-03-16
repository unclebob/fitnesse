// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;

import java.io.File;

public class FileSystemPageTearDown extends Fixture {
  public FileSystemPageTearDown() throws Exception {
    util.FileUtil.deleteFileSystemDirectory(new File(FitnesseFixtureContext.baseDir));
    FitnesseFixtureContext.root = null;

  }
}
