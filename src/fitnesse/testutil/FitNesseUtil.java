// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.IOException;

import util.FileUtil;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContext.Builder;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.VirtualCouplingPage;
import fitnesse.wiki.WikiPage;

public class FitNesseUtil {
  private static FitNesse instance = null;
  public static final int PORT = 1999;
  public static final String URL = "http://localhost:" + PORT + "/";

  public static FitNesseContext startFitnesse(WikiPage root) {
    FitNesseContext context = makeTestContext(root);
    startFitnesseWithContext(context);
    return context;
  }

  public static void startFitnesseWithContext(FitNesseContext context) {
    instance = new FitNesse(context);
    instance.start();
  }

  public static void stopFitnesse() throws IOException {
    instance.stop();
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

  public static void bindVirtualLinkToPage(WikiPage host, WikiPage proxy) {
    VirtualCouplingPage coupling = new VirtualCouplingPage(host, proxy);
    ((VirtualCouplingExtension) host.getExtension(VirtualCouplingExtension.NAME)).setVirtualCoupling(coupling);
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
    return makeTestContext(root, null, SampleFileUtility.base, port);
  }

  public static FitNesseContext makeTestContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port) {
    Builder builder = new Builder();
    builder.root = root;
    builder.rootPath = rootPath;
    builder.rootDirectoryName = rootDirectoryName;
    builder.port = port;
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


  public static void destroyTestContext() {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

}
