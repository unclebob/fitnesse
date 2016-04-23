// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.util.ArrayList;
import java.util.Collection;

import fit.RowFixture;

public class PrimeFactorsFixture extends RowFixture {
  public static class Factor {
    public Factor(int factor) {
      this.factor = factor;
    }

    public int factor;
  }

  @Override
  public Object[] query() {
    int n = Integer.parseInt(args[0]);
    Collection<Factor> factors = new ArrayList<>();
    int f = 2;
    while (n > 1) {
      while (n % f == 0) {
        factors.add(new Factor(f));
        n /= f;
      }
      f++;
    }
    return factors.toArray(new Factor[factors.size()]);
  }

  @Override
  public Class<?> getTargetClass()             // get expected type of row
  {
    return Factor.class;
  }
}
