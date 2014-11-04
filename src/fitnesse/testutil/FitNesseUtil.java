// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.PluginException;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
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
    Properties properties = new Properties();
    properties.setProperty("FITNESSE_PORT", String.valueOf(PORT));
    return makeTestContext(InMemoryPage.makeRoot("RooT", properties));
  }

  public static FitNesseContext makeTestContext(WikiPage root) {
    return makeTestContext(root, PORT);
  }

  public static FitNesseContext makeTestContext(int port) {
    return makeTestContext(InMemoryPage.makeRoot("root"), port);
  }

  public static FitNesseContext makeTestContext(WikiPage root, int port) {
    return makeTestContext(root, ".", FitNesseUtil.base, port, new PromiscuousAuthenticator());
  }

  public static FitNesseContext makeTestContext(WikiPage root,
      Authenticator authenticator) {
    return makeTestContext(root, ".", FitNesseUtil.base, PORT, authenticator);
  }

  public static FitNesseContext makeTestContext(WikiPage root, int port,
      Authenticator authenticator) {
    return makeTestContext(root, ".", FitNesseUtil.base, port, authenticator);
  }



  public static FitNesseContext makeTestContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port) {
    return makeTestContext(root, rootPath, rootDirectoryName, port, null);
  }

  public static FitNesseContext makeTestContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port, Authenticator authenticator) {

    FitNesseContext context;

    try {
      context = ContextConfigurator.systemDefaults()
        .withRoot(root)
        .withRootPath(rootPath)
        .withRootDirectoryName(rootDirectoryName)
        .withPort(port)
        .withAuthenticator(authenticator)
        .withVersionsController(new ZipFileVersionsController())
        .withRecentChanges(new RecentChangesWikiPage())
        .makeFitNesseContext();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } catch (PluginException e) {
      throw new IllegalStateException(e);
    }

    // Ensure Velocity is configured with the default root directory name (FitNesseRoot)
    context.pageFactory.getVelocityEngine();
    return context;
  }

  public static void destroyTestContext() {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

}
