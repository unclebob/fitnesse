// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg;

import fit.ColumnFixture;

public class Division extends ColumnFixture {
  public double numerator;
  public double denominator;

  public double quotient() {
    return numerator / denominator;
  }

  public void setNumerator(double numerator) {
    this.numerator = numerator;
  }

  public void setDenominator(double denominator) {
    this.denominator = denominator;
  }
}
