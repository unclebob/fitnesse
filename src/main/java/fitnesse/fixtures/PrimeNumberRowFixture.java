// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.RowFixture;

public class PrimeNumberRowFixture extends RowFixture {
  public Object[] query() throws Exception {
    PrimeData[] array = new PrimeData[5];
    array[0] = new PrimeData(11);
    array[1] = new PrimeData(5);
    array[2] = new PrimeData(3);
    array[3] = new PrimeData(7);
    array[4] = new PrimeData(2);
    return array;
  }

  public Class<?> getTargetClass() {
    return PrimeData.class;
  }
}
