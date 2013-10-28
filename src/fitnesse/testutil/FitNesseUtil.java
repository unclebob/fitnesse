// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContext.Builder;
import fitnesse.authentication.Authenticator;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.WikiPage;
import util.FileUtil;

import java.io.IOException;
import java.util.Properties;

public class FitNesseUtil {
  public static final String base = "TestDir";
  public static final int PORT = 1999;
  public static final String URL = "http://localhost:" + PORT + "/";

  private static FitNesse instance = null;

  public static FitNesseContext startFitnesse(WikiPage root) {
    FitNesseContext context = makeTestContext(root);
    startFitnesseWithContext(context);
    return context;
  }

  public static void startFitnesseWithContext(FitNesseContext context) {
    instance = context.fitNesse;
    instance.start();
  }

  public static void stopFitnesse() throws IOException {
    instance.stop();
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

  public static FitNesseContext makeTestContext() {
    return makeTestContext(InMemoryPage.makeRoot("root"));
  }

  public static FitNesseContext makeTestContext(WikiPage root) {
    return makeTestContext(root, PORT);
  }

  public static FitNesseContext makeTestContext(int port) {
    return makeTestContext(InMemoryPage.makeRoot("root"), port);
  }

  public static FitNesseContext makeTestContext(WikiPage root, int port) {
    return makeTestContext(root, null, FitNesseUtil.base, port, null);
  }

  public static FitNesseContext makeTestContext(WikiPage root,
      Authenticator authenticator) {
    return makeTestContext(root, null, FitNesseUtil.base, PORT, authenticator);
  }

  public static FitNesseContext makeTestContext(WikiPage root, int port,
      Authenticator authenticator) {
    return makeTestContext(root, null, FitNesseUtil.base, port, authenticator);
  }



  public static FitNesseContext makeTestContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port) {
    return makeTestContext(root, rootPath, rootDirectoryName, port, null);
  }

  public static FitNesseContext makeTestContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port, Authenticator authenticator) {
    Builder builder = new Builder();
    builder.root = root;
    builder.rootPath = rootPath;
    builder.rootDirectoryName = rootDirectoryName;
    builder.port = port;
    builder.authenticator = authenticator;
    builder.versionsController = new ZipFileVersionsController();
    builder.recentChanges = new RecentChangesWikiPage();
    builder.properties = new Properties();
    FitNesseContext context = builder.createFitNesseContext();

    // Ensure Velocity is configured with the default root directory name (FitNesseRoot)
    context.pageFactory.getVelocityEngine();
    return context;
  }

  public static FitNesseContext makeTestContext(FitNesseContext context,
      int port) {
    Builder builder = new Builder(context);
    builder.port = port;
    return builder.createFitNesseContext();
  }

  public static FitNesseContext makeTestContext(FitNesseContext context,
      Authenticator authenticator) {
    Builder builder = new Builder(context);
    builder.authenticator = authenticator;
    return builder.createFitNesseContext();
  }


  public static void destroyTestContext() {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

}
