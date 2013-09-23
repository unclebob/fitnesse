// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.


public class PrimitiveFixture extends Fixture {

  // format converters ////////////////////////

  public static long parseLong(Parse cell) {
    return Long.parseLong(cell.text());
  }

  public static double parseDouble(Parse cell) {
    return Double.parseDouble(cell.text());
  }

  // answer comparisons ///////////////////////

  public void check(Parse cell, String value) {
    if (cell.text().equals(value)) {
      right(cell);
    } else {
      wrong(cell, value);
    }
  }

  public void check(Parse cell, long value) {
    if (parseLong(cell) == value) {
      right(cell);
    } else {
      wrong(cell, Long.toString(value));
    }
  }

  public void check(Parse cell, double value) {
    if (parseDouble(cell) == value) {
      right(cell);
    } else {
      wrong(cell, Double.toString(value));
    }
  }

}
