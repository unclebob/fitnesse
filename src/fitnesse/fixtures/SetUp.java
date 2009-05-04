// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import static fitnesse.fixtures.FitnesseFixtureContext.*;
import fitnesse.components.SaveRecorder;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;

import java.io.File;

import util.FileUtil;

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
    fitnesse = new FitNesse(context, false);
    File historyDirectory = context.getTestHistoryDirectory();
    if (historyDirectory.exists())
      FileUtil.deleteFileSystemDirectory(historyDirectory);
    historyDirectory.mkdirs();
    SaveRecorder.clear();
    fitnesse.start();
  }
}
