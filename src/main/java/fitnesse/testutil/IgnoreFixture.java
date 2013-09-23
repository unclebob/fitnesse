// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

public class IgnoreFixture extends Fixture {
  public void doTable(Parse table) {
    ignore(table.parts.parts);
  }
}
