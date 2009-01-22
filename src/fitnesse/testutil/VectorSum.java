// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.ColumnFixture;

public class VectorSum extends ColumnFixture {
  public CartesianVector v1;
  public CartesianVector v2;

  public CartesianVector sum() {
    return v1.add(v2);
  }
}
