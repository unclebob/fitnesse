// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;

public class CountFixture extends Fixture {
  private int counter = 0;

  public void count() {
    counter++;
  }

  public int counter() {
    return counter;
  }

  public void counter(int i) {
    counter = i;
  }
}


