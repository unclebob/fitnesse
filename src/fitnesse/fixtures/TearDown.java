// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;

public class TearDown extends Fixture {
  public TearDown() throws Exception {
    FitnesseFixtureContext.fitnesse.stop();
    FitnesseFixtureContext.root = null;
  }
}
