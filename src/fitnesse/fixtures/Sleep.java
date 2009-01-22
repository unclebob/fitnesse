// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import fit.Parse;

public class Sleep extends Fixture {
  public void doTable(Parse table) {
    String args[] = getArgs();
    long millis = Long.parseLong(args[0]);
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
    }
  }
}
