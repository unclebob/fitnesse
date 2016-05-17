// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

// Used in acceptance suite
public class PassFixture extends Fixture {
  @Override
  public void doTable(Parse parse) {
    right(parse);
  }
}
