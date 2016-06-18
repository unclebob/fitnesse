// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Properties;

import fit.Fixture;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.SaveRecorder;
import fitnesse.testutil.FitNesseUtil;
import util.FileUtil;

import static fitnesse.fixtures.FitnesseFixtureContext.context;

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
        return FitnesseFixtureContext.authenticator == null || FitnesseFixtureContext.authenticator.isAuthenticated(username, password);
      }
    }, properties);
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    historyDirectory.mkdirs();
    SaveRecorder.clear();
    FitNesseUtil.startFitnesseWithContext(context);
  }

  private static Properties asProperties(String configuration) throws Exception {
    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(configuration.getBytes(FileUtil.CHARENCODING)));
    return properties;
  }
}
