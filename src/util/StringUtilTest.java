// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {
  @Test
  public void testCombineArraysBothEmpty() {
    assertEquals(0, StringUtil.combineArrays(new String[]{}, new String[]{}).length);
  }

  @Test
  public void testCombineArraysWithOneItemInFirst() {
    String[] first = new String[]{"a"};
    String[] result = StringUtil.combineArrays(first, new String[]{});
    assertEquals(1, result.length);
    assertEquals("a", result[0]);
  }

  @Test
  public void testCombineArraysWithOneItemInEach() {
    String[] first = new String[]{"a"};
    String[] second = new String[]{"b"};
    String[] result = StringUtil.combineArrays(first, second);
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }

  @Test
  public void testCombineArraysWithMixedNumbers() {
    String[] first = new String[]{"a", "b", "c"};
    String[] second = new String[]{"d", "e"};
    String[] third = new String[]{"f", "g", "h"};
    String[] result = StringUtil.combineArrays(first, second, third);
    assertEquals(8, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
    assertEquals("c", result[2]);
    assertEquals("d", result[3]);
    assertEquals("e", result[4]);
    assertEquals("f", result[5]);
    assertEquals("g", result[6]);
    assertEquals("h", result[7]);
  }

  @Test
  public void testTrimNullStringReturnsNull() {
    assertEquals(null, StringUtil.trimNonNullString(null));
  }

  @Test
  public void testTrimAllSpacesStringResultsInEmptyString() {
    assertEquals("", StringUtil.trimNonNullString("   "));
  }

  @Test
  public void testTrimStringWithLeadingAndTrailingSpaces() {
    assertEquals("FitNesse", StringUtil.trimNonNullString(" FitNesse "));
  }

  @Test
  public void replaceAll() throws Exception {
    assertEquals("my name is Bob, Bob is my name", StringUtil.replaceAll("my name is $name, $name is my name", "$name", "Bob"));
    assertEquals("_$namex_", StringUtil.replaceAll("_$name_", "$name", "$namex"));

  }

}
