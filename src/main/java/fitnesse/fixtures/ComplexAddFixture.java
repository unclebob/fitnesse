// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class ComplexAddFixture extends ColumnFixture {
  public int[] a;
  public int[] b;

  public int[] sum() {
    return new int[]{a[0] + b[0], a[1] + b[1]};
  }
}
