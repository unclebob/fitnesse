// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.TestCase;

public class ScientificDoubleTest extends TestCase {
  public void testScientificDouble() {
    Double pi = new Double(3.141592653589793);
    assertEquals(ScientificDouble.valueOf("3.14"), pi);
    assertEquals(ScientificDouble.valueOf("3.142"), pi);
    assertEquals(ScientificDouble.valueOf("3.1416"), pi);
    assertEquals(ScientificDouble.valueOf("3.14159"), pi);
    assertEquals(ScientificDouble.valueOf("3.141592653589793"), pi);
    assertFalse(ScientificDouble.valueOf("3.140").equals(pi));
    assertFalse(ScientificDouble.valueOf("3.144").equals(pi));
    assertFalse(ScientificDouble.valueOf("3.1414").equals(pi));
    assertFalse(ScientificDouble.valueOf("3.141592863").equals(pi));
    assertEquals(ScientificDouble.valueOf("6.02e23"), new Double(6.02e23));
    assertEquals(ScientificDouble.valueOf("6.02E23"), new Double(6.024E23));
    assertEquals(ScientificDouble.valueOf("6.02e23"), new Double(6.016e23));
    assertFalse(ScientificDouble.valueOf("6.02e23").equals(new Double(6.026e23)));
    assertFalse(ScientificDouble.valueOf("6.02e23").equals(new Double(6.014e23)));
  }

}
