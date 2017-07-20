// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.plugins.PluginException;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wiki.fs.InMemoryPage;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class FitNesseUtil {
  public static final String base = "RooT";
  public static final int PORT = 1999;
  public static final String URL = "http://localhost:" + PORT + "/";

  private static FitNesse instance = null;

  public static void startFitnesseWithContext(FitNesseContext context) throws IOException {
    instance = context.fitNesse;
    instance.start(new PlainServerSocketFactory().createServerSocket(context.port));
  }

  public static void stopFitnesse() throws IOException {
    instance.stop();
    FileUtil.deleteFileSystemDirectory(base);
  }

  public static FitNesseContext makeTestContext() {
    Properties properties = new Properties();
    properties.setProperty("FITNESSE_PORT", String.valueOf(PORT));
    return makeTestContext(InMemoryPage.newInstance(), properties);
  }


  public static FitNesseContext makeTestContext(Properties properties) {
    return makeTestContext(InMemoryPage.newInstance(), properties);
  }

  public static FitNesseContext makeTestContext(WikiPageFactory wikiPageFactory, Properties properties) {
    File temporaryFolder = createTemporaryFolder();
    return makeTestContext(wikiPageFactory, temporaryFolder.getPath(), FitNesseUtil.base, PORT, new PromiscuousAuthenticator(), properties);
  }

  public static FitNesseContext makeTestContext(int port) {
    return makeTestContext(InMemoryPage.newInstance(), createTemporaryFolder().getPath(), FitNesseUtil.base, port, new PromiscuousAuthenticator());
  }

  public static FitNesseContext makeTestContext(Authenticator authenticator) {
    return makeTestContext(InMemoryPage.newInstance(), createTemporaryFolder().getPath(), FitNesseUtil.base, PORT, authenticator);
  }

  public static FitNesseContext makeTestContext(FileSystem fileSystem) {
    return makeTestContext(InMemoryPage.newInstance(fileSystem), createTemporaryFolder().getPath(), FitNesseUtil.base, PORT, new PromiscuousAuthenticator());
  }

  public static FitNesseContext makeTestContext(int port, Authenticator authenticator, Properties properties) {
    return makeTestContext(InMemoryPage.newInstance(), createTemporaryFolder().getPath(), FitNesseUtil.base, port, authenticator, properties);
  }

  public static FitNesseContext makeTestContext(WikiPageFactory wikiPageFactory, String rootPath, String name, int port) {
    return makeTestContext(wikiPageFactory, rootPath, name, port, new PromiscuousAuthenticator());
  }

  public static FitNesseContext makeTestContext(WikiPageFactory wikiPageFactory, String rootPath,
                                                String rootDirectoryName, int port, Authenticator authenticator) {
    return makeTestContext(wikiPageFactory, rootPath, rootDirectoryName, port, authenticator, new Properties());
  }

  public static FitNesseContext makeTestContext(WikiPageFactory wikiPageFactory, String rootPath,
                                                String rootDirectoryName, int port, Authenticator authenticator, Properties properties) {
    FitNesseContext context;

    try {
      context = ContextConfigurator.systemDefaults()
              .withWikiPageFactory(wikiPageFactory)
              .withRootPath(rootPath)
              .withRootDirectoryName(rootDirectoryName)
              .withPort(port)
              .withAuthenticator(authenticator)
              .withVersionsController(new ZipFileVersionsController())
              .withRecentChanges(new RecentChangesWikiPage())
              .updatedWith(properties)
              .makeFitNesseContext();
    } catch (IOException | PluginException e) {
      throw new IllegalStateException(e);
    }

    // Ensure Velocity is configured with the default root directory name (FitNesseRoot)
    context.pageFactory.getVelocityEngine();
    return context;
  }

  public static File createTemporaryFolder() {
    File createdFolder;
    try {
      createdFolder = File.createTempFile("fitnesse", "");
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create temporary folder for test execution", e);
    }
    createdFolder.delete();
    createdFolder.mkdir();
    return createdFolder;
  }

  public static void destroyTestContext(FitNesseContext context) throws IOException {
    FileUtil.deleteFileSystemDirectory(context.rootPath);
  }

  public static void destroyTestContext() throws IOException {
    FileUtil.deleteFileSystemDirectory(FitNesseUtil.base);
  }
}
