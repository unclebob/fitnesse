// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

// Warning: not (yet) a general number usable in all calculations.

public class ScientificDouble extends Number implements Comparable<Number> {
  private static final long serialVersionUID = 1L;

  protected double value;
  protected double precision;

  public ScientificDouble(double value) {
    this.value = value;
    this.precision = 0;
  }

  public static ScientificDouble valueOf(String s) {
    ScientificDouble result = new ScientificDouble(Double.parseDouble(s));
    result.precision = precision(s);
    return result;
  }

  public static ScientificDouble parse(String s) {
    return valueOf(s);
  }

  public static double precision(String s) {
    double value = Double.parseDouble(s);
    double bound = Double.parseDouble(tweak(s.trim()));
    return Math.abs(bound - value);
  }

  public static String tweak(String s) {
    int pos;
    if ((pos = s.toLowerCase().indexOf("e")) >= 0) {
      return tweak(s.substring(0, pos)) + s.substring(pos);
    }
    if (s.indexOf(".") >= 0) {
      return s + "5";
    }
    return s + ".5";
  }

  public boolean equals(Object obj) {
    return compareTo((Number) obj) == 0;
  }

  public int compareTo(Number obj) {
    double other = obj.doubleValue();
    double diff = value - other;
    if (diff < -precision) return -1;
    if (diff > precision) return 1;
    if (Double.isNaN(value) && Double.isNaN(other)) return 0;
    if (Double.isNaN(value)) return 1;
    if (Double.isNaN(other)) return -1;
    return 0;
  }

  public String toString() {
    return Double.toString(value);
  }

  public double doubleValue() {
    return value;
  }

  public float floatValue() {
    return (float) value;
  }

  public long longValue() {
    return (long) value;
  }

  public int intValue() {
    return (int) value;
  }
}
