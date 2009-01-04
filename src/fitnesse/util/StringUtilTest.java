// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

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
    String[] result = StringUtil.combineArrays(first, second);
    assertEquals(5, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
    assertEquals("c", result[2]);
    assertEquals("d", result[3]);
    assertEquals("e", result[4]);
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
