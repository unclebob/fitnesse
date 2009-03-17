// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class TypeAdapterTest extends TestCase {
  private TestFixture f = new TestFixture();
  private TypeAdapter adapter;
  private static final String dateFormat = "MMM dd yyyy";

  public void testTypeAdapter() throws Exception {

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleInt"));
    adapter.set(adapter.parse("123456"));
    assertEquals(123456, f.sampleInt);
    assertEquals("-234567", adapter.parse("-234567").toString());

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleInteger"));
    adapter.set(adapter.parse("54321"));
    assertEquals("54321", f.sampleInteger.toString());

    adapter = TypeAdapter.on(f, f.getClass().getMethod("pi", new Class[]{}));
    assertEquals(3.14159, ((Double) adapter.invoke()).doubleValue(), 0.00001);
    assertEquals(new Double(3.141592653), adapter.invoke());

    adapter = TypeAdapter.on(f, f.getClass().getField("ch"));
    adapter.set(adapter.parse("abc"));
    assertEquals('a', f.ch);

    adapter = TypeAdapter.on(f, f.getClass().getField("name"));
    adapter.set(adapter.parse("xyzzy"));
    assertEquals("xyzzy", f.name);

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleFloat"));
    adapter.set(adapter.parse("6.02e23"));
    assertEquals(6.02e23, f.sampleFloat, 1e17);

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleArray"));
    adapter.set(adapter.parse("1,2,3"));
    assertEquals(1, f.sampleArray[0]);
    assertEquals(2, f.sampleArray[1]);
    assertEquals(3, f.sampleArray[2]);
    assertEquals("1, 2, 3", adapter.toString(f.sampleArray));
    assertTrue(adapter.equals(new int[]
      {1, 2, 3}, f.sampleArray));

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleDate"));
    Date date = new GregorianCalendar(49 + 1900, 4, 26).getTime();
    String format = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
    adapter.set(adapter.parse(format));
    assertEquals(date, f.sampleDate);

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleByte"));
    adapter.set(adapter.parse("123"));
    assertEquals(123, f.sampleByte);

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleShort"));
    adapter.set(adapter.parse("12345"));
    assertEquals(12345, f.sampleShort);
  }

  static class TestFixture extends ColumnFixture {
    public byte sampleByte;
    public short sampleShort;
    public int sampleInt;
    public Integer sampleInteger;
    public float sampleFloat;
    public char ch;
    public String name;
    public int[] sampleArray;
    public Date sampleDate;

    public double pi() {
      return 3.141592653;
    }

    public Integer getNull() {
      return null;
    }
  }

  public void testBooleanTypeAdapter() throws Exception {
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

  private void assertBooleanTypeAdapterParses(String booleanString, boolean assertedValue) throws Exception {
    TypeAdapter booleanAdapter = TypeAdapter.adapterFor(Boolean.class);
    Boolean result = (Boolean) booleanAdapter.parse(booleanString);
    assertTrue(result.booleanValue() == assertedValue);
  }

  public void testParseDelegateObjectMethod() throws Exception {
    Date april26Of1949 = new GregorianCalendar(49 + 1900, 4, 26).getTime();
    String format = new SimpleDateFormat(dateFormat).format(april26Of1949);

    TypeAdapter.registerParseDelegate(Date.class, new SimpleDateFormat(dateFormat));

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleDate"));
    adapter.set(adapter.parse(format));
    assertEquals(april26Of1949, f.sampleDate);
  }

  public void testParseDelegateClassMethod() throws Exception {
    Date april26Of1949 = new GregorianCalendar(49 + 1900, 4, 26).getTime();
    String format = new SimpleDateFormat(dateFormat).format(april26Of1949);

    TypeAdapter.registerParseDelegate(Date.class, DateFormater.class);

    adapter = TypeAdapter.on(f, f.getClass().getField("sampleDate"));
    adapter.set(adapter.parse(format));
    assertEquals(april26Of1949, f.sampleDate);
  }

  public void testParsedelegateClassShouldHavePublicStaticNonVoidParseMethodWithStringParam() throws Exception {
    TypeAdapter.registerParseDelegate(Class.class, PublicStaticParseMethod.class);
  }

  public void testShouldThrowNoSuchMethodExceptionIfGivenParseDelgateClassDoesNotHavePublicParseMethod()
    throws Exception {
    try {
      TypeAdapter.registerParseDelegate(Class.class, ProtectedParseMethod.class);
    } catch (RuntimeException e) {
      assertEquals("Parse delegate class " + ProtectedParseMethod.class.getName()
        + " does not have a suitable static parse() method.", e.getMessage());
    }
  }

  public void testShouldThrowNoSuchMethodExceptionIfGivenParseDelgateClassDoesNotHaveStaticParseMethod()
    throws Exception {
    try {
      TypeAdapter.registerParseDelegate(Class.class, PublicNonStaticParseMethod.class);
    } catch (RuntimeException e) {
      assertEquals("Parse delegate class " + PublicNonStaticParseMethod.class.getName()
        + " does not have a suitable static parse() method.", e.getMessage());
    }
  }

  public void testShouldThrowNoSuchMethodExceptionIfGivenParseDelgateClassHasParseMethodReturningVoid()
    throws Exception {
    try {
      TypeAdapter.registerParseDelegate(Class.class, PublicStaticVoidParseMethod.class);
    } catch (RuntimeException e) {
      assertEquals("Parse delegate class " + PublicStaticVoidParseMethod.class.getName()
        + " does not have a suitable static parse() method.", e.getMessage());
    }
  }

  public void testShouldThrowNoSuchMethodExceptionIfGivenParseDelgateClassDoesNotHaveParseMethodWithStringParam()
    throws Exception {
    try {
      TypeAdapter.registerParseDelegate(Class.class, PublicStaticParseMethodWithoutStringParam.class);
    } catch (RuntimeException e) {
      assertEquals("Parse delegate class " + PublicStaticParseMethodWithoutStringParam.class.getName()
        + " does not have a suitable static parse() method.", e.getMessage());
    }
  }

  public static class DateFormater {
    public static Date parse(String date) throws ParseException {
      return new SimpleDateFormat(dateFormat).parse(date);
    }
  }

  public static class ProtectedParseMethod {
    protected static ProtectedParseMethod parse(String a) {
      return null;
    }
  }

  public static class PublicNonStaticParseMethod {
    public ProtectedParseMethod parse(String a) {
      return null;
    }
  }

  public static class PublicStaticVoidParseMethod {
    public static void parse(String a) {
    }
  }

  public static class PublicStaticParseMethod {
    public static ProtectedParseMethod parse(String a) {
      return new ProtectedParseMethod();
    }
  }

  public static class PublicStaticParseMethodWithoutStringParam {
    public static PublicStaticParseMethodWithoutStringParam parse() {
      return null;
    }
  }

  @Override
  protected void tearDown() throws Exception {
    TypeAdapter.clearDelegatesForNextTest();
  }
}
