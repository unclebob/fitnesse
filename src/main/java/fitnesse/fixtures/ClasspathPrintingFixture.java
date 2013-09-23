// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import fit.Parse;

public class ClasspathPrintingFixture extends Fixture {
  public void doTable(Parse table) {
    table.parts.parts.addToBody("<br/>classpath: " + System.getProperty("java.class.path"));
  }
}
