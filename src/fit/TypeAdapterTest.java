// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.TestCase;
import java.util.*;
import java.text.DateFormat;

public class TypeAdapterTest extends TestCase
{
  public void testTypeAdapter() throws Exception
  {
    TestFixture f = new TestFixture();
    TypeAdapter a = TypeAdapter.on(f, f.getClass().getField("sampleInt"));
    a.set(a.parse("123456"));
    assertEquals(123456, f.sampleInt);
    assertEquals("-234567", a.parse("-234567").toString());
    a = TypeAdapter.on(f, f.getClass().getField("sampleInteger"));
    a.set(a.parse("54321"));
    assertEquals("54321", f.sampleInteger.toString());
    a = TypeAdapter.on(f, f.getClass().getMethod("pi", new Class[]{}));
    assertEquals(3.14159, ((Double) a.invoke()).doubleValue(), 0.00001);
    assertEquals(new Double(3.141592653), a.invoke());
    a = TypeAdapter.on(f, f.getClass().getField("ch"));
    a.set(a.parse("abc"));
    assertEquals('a', f.ch);
    a = TypeAdapter.on(f, f.getClass().getField("name"));
    a.set(a.parse("xyzzy"));
    assertEquals("xyzzy", f.name);
    a = TypeAdapter.on(f, f.getClass().getField("sampleFloat"));
    a.set(a.parse("6.02e23"));
    assertEquals(6.02e23, f.sampleFloat, 1e17);
    a = TypeAdapter.on(f, f.getClass().getField("sampleArray"));
    a.set(a.parse("1,2,3"));
    assertEquals(1, f.sampleArray[0]);
    assertEquals(2, f.sampleArray[1]);
    assertEquals(3, f.sampleArray[2]);
    assertEquals("1, 2, 3", a.toString(f.sampleArray));
    assertTrue(a.equals(new int[]{1, 2, 3}, f.sampleArray));
    a = TypeAdapter.on(f, f.getClass().getField("sampleDate"));
    Date date = new GregorianCalendar(49 + 1900, 4, 26).getTime();
    a.set(a.parse(DateFormat.getDateInstance(DateFormat.SHORT).format(date)));
    assertEquals(date, f.sampleDate);
    a = TypeAdapter.on(f, f.getClass().getField("sampleByte"));
    a.set(a.parse("123"));
    assertEquals(123, f.sampleByte);
    a = TypeAdapter.on(f, f.getClass().getField("sampleShort"));
    a.set(a.parse("12345"));
    assertEquals(12345, f.sampleShort);
  }

  static class TestFixture extends ColumnFixture
  {
    public byte sampleByte;
    public short sampleShort;
    public int sampleInt;
    public Integer sampleInteger;
    public float sampleFloat;
    public char ch;
    public String name;
    public int[] sampleArray;
    public Date sampleDate;

    public double pi()
    {
      return 3.141592653;
    }
  }

  public void testBooleanTypeAdapter() throws Exception
  {
    assertBooleanTypeAdapterParses("true", true);
    assertBooleanTypeAdapterParses("yes", true);
    assertBooleanTypeAdapterParses("y", true);
    assertBooleanTypeAdapterParses("+", true);
    assertBooleanTypeAdapterParses("1", true);
    assertBooleanTypeAdapterParses("True", true);
    assertBooleanTypeAdapterParses("YES", true);
    assertBooleanTypeAdapterParses("Y", true);

    assertBooleanTypeAdapterParses("N", false);
    assertBooleanTypeAdapterParses("No", false);
    assertBooleanTypeAdapterParses("false", false);
    assertBooleanTypeAdapterParses("0", false);
    assertBooleanTypeAdapterParses("-", false);
    assertBooleanTypeAdapterParses("whatever", false);
  }

  private void assertBooleanTypeAdapterParses(String booleanString, boolean assertedValue) throws Exception
  {
    TypeAdapter booleanAdapter = TypeAdapter.adapterFor(Boolean.class);
    Boolean result = (Boolean) booleanAdapter.parse(booleanString);
    assertTrue(result.booleanValue() == assertedValue);
  }
}
