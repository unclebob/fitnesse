// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import static fitnesse.fixtures.FitnesseFixtureContext.context;
import static fitnesse.fixtures.FitnesseFixtureContext.root;

import java.io.File;
import java.util.Properties;

import util.FileUtil;
import fit.Fixture;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;

public class SetUp extends Fixture {
  public SetUp() throws Exception {
    final int port = 9123;
    Properties properties = new Properties();
    properties.setProperty("FITNESSE_PORT", String.valueOf(port));
    root = InMemoryPage.makeRoot("RooT", properties);
    context = FitNesseUtil.makeTestContext(root, port, new Authenticator() {
      @Override public boolean isAuthenticated(String username, String password) {
        if (FitnesseFixtureContext.authenticator != null) {
          return FitnesseFixtureContext.authenticator.isAuthenticated(username, password);
        }
        return true;
      }
    });
    context.fitNesse.dontMakeDirs();
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    historyDirectory.mkdirs();
    SaveRecorder.clear();
    context.fitNesse.start();
  }
}
