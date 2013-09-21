// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class FitNesseStatus extends ColumnFixture {
  public boolean isRunning() {
    return FitnesseFixtureContext.fitnesse.isRunning();
  }
}
