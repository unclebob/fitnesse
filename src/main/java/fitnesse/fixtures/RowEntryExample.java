// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

public class RowEntryExample extends RowEntryFixture {
  public int v;

  public void enterRow() throws Exception {
    if (v == 0)
      throw new Exception("Oh, no!  Zero!");
  }
}
