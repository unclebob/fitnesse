// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import junit.framework.TestCase;

public class CartesianVectorTest extends TestCase {
  public void testParseVector() throws Exception {
    CartesianVector v = CartesianVector.parse("(0,0)");
    assertEquals(0.0, v.getX(), .001);
    assertEquals(0.0, v.getY(), .001);
  }

  public void testVectorEquals() throws Exception {
    CartesianVector v1 = new CartesianVector(3.1, -5.2);
    CartesianVector v2 = new CartesianVector(3.1, -5.2);
    CartesianVector v3 = new CartesianVector(0, 0);
    assertTrue(v1.equals(v2));
    assertFalse(v1.equals(v3));
  }

  public void testAdd() throws Exception {
    CartesianVector v1 = new CartesianVector(3, 4);
    CartesianVector v2 = new CartesianVector(1, 2);
    CartesianVector sum = v1.add(v2);
    assertEquals(sum, new CartesianVector(4, 6));
  }
}
