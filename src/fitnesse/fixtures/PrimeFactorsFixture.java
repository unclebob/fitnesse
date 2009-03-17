// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.util.ArrayList;

import fit.RowFixture;

public class PrimeFactorsFixture extends RowFixture {
  public static class Factor {
    public Factor(int factor) {
      this.factor = factor;
    }

    public int factor;
  }

  public Object[] query() {
    int n = Integer.parseInt(args[0]);
    ArrayList<Factor> factors = new ArrayList<Factor>();
    for (int f = 2; n > 1; f++)
      for (; n % f == 0; n /= f)
        factors.add(new Factor(f));
    return factors.toArray(new Factor[0]);
  }

  public Class<?> getTargetClass()             // get expected type of row
  {
    return Factor.class;
  }
}
