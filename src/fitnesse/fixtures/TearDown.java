// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;

import java.io.File;

import static fitnesse.fixtures.FitnesseFixtureContext.*;
import util.Clock;
import util.FileUtil;

public class TearDown extends Fixture {
  public TearDown() throws Exception {
    context.fitNesse.stop();
    root = null;
    authenticator = null;
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    Clock.restoreDefaultClock();
    context = null;
  }
}
