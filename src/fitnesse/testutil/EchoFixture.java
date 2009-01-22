// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

public class EchoFixture extends Fixture {
  public void doTable(Parse table) {
    String text = getArgs()[0];
    table.parts.parts = new Parse("tr", "", new Parse("td", text, null, null), null);
  }
}
