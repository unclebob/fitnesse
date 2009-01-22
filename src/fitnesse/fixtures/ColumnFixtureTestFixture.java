// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class ColumnFixtureTestFixture extends ColumnFixture {
  public int input;

  public int output() {
    return input;
  }

  public Integer integerInput;

  public Integer integerOutput() {
    return integerInput;
  }

  public boolean exception() throws Exception {
    throw new Exception("I thowed up");
  }
}
