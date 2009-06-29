// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.components.SaveRecorder;
import static fitnesse.fixtures.FitnesseFixtureContext.*;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;
import util.FileUtil;

import java.io.File;

public class SetUp extends Fixture {
  public SetUp() throws Exception {
    //TODO - MdM - There's got to be a better way.
    WikiImportTestEventListener.register();

    root = InMemoryPage.makeRoot("RooT");
    responderFactory = new ResponderFactory(baseDir + "/RooT/");
    context = new FitNesseContext(root);
    context.responderFactory = responderFactory;
    context.port = 9123;
    context.rootPagePath = baseDir;
    VelocityFactory.makeVelocityFactory(context);
    fitnesse = new FitNesse(context, false);
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    historyDirectory.mkdirs();
    SaveRecorder.clear();
    fitnesse.start();
  }
}
