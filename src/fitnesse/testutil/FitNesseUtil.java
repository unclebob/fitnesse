// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.IOException;

import util.FileUtil;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.VirtualCouplingPage;
import fitnesse.wiki.WikiPage;

public class FitNesseUtil {
  private static FitNesse instance = null;
  public static final int port = 1999;
  public static FitNesseContext context;
  public static final String URL = "http://localhost:" + port + "/";

  public static void startFitnesse(WikiPage root) {
    context = makeTestContext(root);
    context.responderFactory = new ResponderFactory(context.rootPagePath);
    context.port = port;
    startFitnesseWithContext(context);
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
    FitNesseContext context = new FitNesseContext(root);
    // Ensure Velocity is configured with the default root directory name (FitNesseRoot)
    context.pageFactory.getVelocityEngine();
    context.rootDirectoryName = "TestDir";
    context.setRootPagePath();
    return context;
  }

  public static void destroyTestContext() {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }
}
