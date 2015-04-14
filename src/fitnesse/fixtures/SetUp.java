// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import static fitnesse.fixtures.FitnesseFixtureContext.context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Properties;

import util.FileUtil;
import fit.Fixture;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.testutil.FitNesseUtil;

public class SetUp extends Fixture {
  public SetUp() throws Exception {
    this(new Properties());
  }

  public SetUp(String configuration) throws Exception {
    this(asProperties(configuration));
  }

  private SetUp(Properties properties) throws Exception {
    final int port = 9123;
    properties.setProperty("FITNESSE_PORT", String.valueOf(port));
    context = FitNesseUtil.makeTestContext(port, new Authenticator() {
      @Override public boolean isAuthenticated(String username, String password) {
        if (FitnesseFixtureContext.authenticator != null) {
          return FitnesseFixtureContext.authenticator.isAuthenticated(username, password);
        }
        return true;
      }
    }, properties);
    context.fitNesse.dontMakeDirs();
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    historyDirectory.mkdirs();
    SaveRecorder.clear();
    context.fitNesse.start();
  }

  private static Properties asProperties(String configuration) throws Exception {
    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(configuration.getBytes(FileUtil.CHARENCODING)));
    return properties;
  }
}
