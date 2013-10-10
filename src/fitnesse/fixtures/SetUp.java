// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import static fitnesse.fixtures.FitnesseFixtureContext.context;
import static fitnesse.fixtures.FitnesseFixtureContext.root;

import java.io.File;

import util.FileUtil;
import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;

public class SetUp extends Fixture {
  public SetUp() throws Exception {
    //TODO - MdM - There's got to be a better way.
    WikiImportTestEventListener.register();

    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root, 9123, new Authenticator() {
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
