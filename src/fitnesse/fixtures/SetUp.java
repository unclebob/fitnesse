// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.components.SaveRecorder;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;

public class SetUp extends Fixture {
  public SetUp() throws Exception {
    //TODO - MdM - There's got to be a better way.
    WikiImportTestEventListener.register();

    FitnesseFixtureContext.root = InMemoryPage.makeRoot("RooT");
    FitnesseFixtureContext.responderFactory = new ResponderFactory(FitnesseFixtureContext.baseDir + "/RooT/");
    FitnesseFixtureContext.context = new FitNesseContext(FitnesseFixtureContext.root);
    FitnesseFixtureContext.context.responderFactory = FitnesseFixtureContext.responderFactory;
    FitnesseFixtureContext.context.port = 9123;
    FitnesseFixtureContext.context.rootPagePath = FitnesseFixtureContext.baseDir;
    FitnesseFixtureContext.fitnesse = new FitNesse(FitnesseFixtureContext.context, false);
    SaveRecorder.clear();
    FitnesseFixtureContext.fitnesse.start();
  }
}
