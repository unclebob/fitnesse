// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.Fixture;
import fit.Parse;

public class OutputWritingFixture extends Fixture {
  public void doTable(Parse parse) {
    Parse cell = parse.parts.more.parts;
    String value = cell.text();
    System.out.println(value);
    right(cell);
  }
}
