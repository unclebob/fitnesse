// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;

public class DelayedCountFixture extends Fixture {
  private int counter = 0;

  public void count() throws Exception {
    counter++;
    Thread.sleep(1000 + (long) (Math.random() * 500.0));
  }

  public int counter() {
    return counter;
  }

  public void counter(int i) {
    counter = i;
  }
}
