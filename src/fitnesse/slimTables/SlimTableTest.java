// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static fitnesse.slimTables.SlimTable.approximatelyEqual;
import static fitnesse.slimTables.SlimTable.Disgracer.disgraceClassName;
import static fitnesse.slimTables.SlimTable.Disgracer.disgraceMethodName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SlimTableTest {
  @Test
  public void gracefulClassNames() throws Exception {
    assertEquals("MyClass", disgraceClassName("my class"));
    assertEquals("myclass", disgraceClassName("myclass"));
    assertEquals("x.y", disgraceClassName("x.y"));
    assertEquals("x_y", disgraceClassName("x_y"));
    assertEquals("MeAndMrs_jones", disgraceClassName("me and mrs_jones"));
    assertEquals("PageCreator", disgraceClassName("Page creator."));
  }

  @Test
  public void gracefulMethodNames() throws Exception {
    assertEquals("myMethodName", disgraceMethodName("my method name"));
    assertEquals("myMethodName", disgraceMethodName("myMethodName"));
    assertEquals("my_method_name", disgraceMethodName("my_method_name"));
    assertEquals("getStringArgs", disgraceMethodName("getStringArgs"));
    assertEquals("setMyVariableName", disgraceMethodName("set myVariableName"));
  }

  @Test
  public void trulyEqual() throws Exception {
    assertTrue(approximatelyEqual("3.0", "3.0"));
  }

  @Test
  public void veryUnequal() throws Exception {
    assertFalse(approximatelyEqual("5", "3"));
  }

  @Test
  public void isWithinPrecision() throws Exception {
    assertTrue(approximatelyEqual("3", "2.5"));
  }

  @Test
  public void justTooBig() throws Exception {
    assertFalse(approximatelyEqual("3.000", "3.0005"));
  }

  @Test
  public void justTooSmall() throws Exception {
    assertFalse(approximatelyEqual("3.0000", "2.999949"));
  }

  @Test
  public void justSmallEnough() throws Exception {
    assertTrue(approximatelyEqual("-3.00", "-2.995"));
  }

  @Test
  public void justBigEnough() throws Exception {
    assertTrue(approximatelyEqual("-3.000000", "-3.000000499"));
  }

  @Test
  public void classicRoundUp() throws Exception {
    assertTrue(approximatelyEqual("3.05", "3.049"));
  }
}
