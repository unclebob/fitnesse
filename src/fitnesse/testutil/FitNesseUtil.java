// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import util.FileUtil;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.VirtualCouplingPage;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.app.VelocityEngine;

public class FitNesseUtil {
  private static FitNesse instance = null;
  public static final int port = 1999;
  public static FitNesseContext context;
  public static final String URL = "http://localhost:" + port + "/";

  public static void startFitnesse(WikiPage root) throws Exception {
    context = new FitNesseContext();
    context.root = root;
    context.port = port;
    context.rootPath = "TestDir";
    context.rootPageName = root.getName();
    context.rootPagePath = context.rootPath + "/" + context.rootPageName;
    context.responderFactory = new ResponderFactory(context.rootPagePath);
    VelocityEngine engine = new VelocityEngine();
    engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, "FitNesseRoot/files/templates");
    context.setVelocityEngine(engine);
    instance = new FitNesse(context);
    instance.start();
  }

  public static void stopFitnesse() throws Exception {
    instance.stop();
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

  public static void bindVirtualLinkToPage(WikiPage host, WikiPage proxy) throws Exception {
    VirtualCouplingPage coupling = new VirtualCouplingPage(host, proxy);
    ((VirtualCouplingExtension) host.getExtension(VirtualCouplingExtension.NAME)).setVirtualCoupling(coupling);
  }
}
