// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.Fixture;
import fitnesse.wiki.InMemoryPage;
import fitnesse.responders.ResponderFactory;
import fitnesse.*;

public class SetUp extends Fixture
{
  public SetUp() throws Exception
  {
    FitnesseFixtureContext.root = InMemoryPage.makeRoot("RooT");
    FitnesseFixtureContext.responderFactory = new ResponderFactory(FitnesseFixtureContext.baseDir + "/RooT/");
    FitnesseFixtureContext.context = new FitNesseContext(FitnesseFixtureContext.root);
	  FitnesseFixtureContext.context.responderFactory = FitnesseFixtureContext.responderFactory;
    FitnesseFixtureContext.context.port = 9123;
    FitnesseFixtureContext.context.rootPagePath = FitnesseFixtureContext.baseDir;
	  FitnesseFixtureContext.fitnesse = new FitNesse(FitnesseFixtureContext.context, false);

	  FitnesseFixtureContext.fitnesse.start();
  }
}
