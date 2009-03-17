package fit.decorator.util;

import junit.framework.TestCase;
import fit.decorator.exceptions.InvalidInputException;

public class DeltaTest extends TestCase {

  private static final String INT_DATA_TYPE = "int";
  private static final String DOUBLE_DATA_TYPE = "double";
  private static final String STRING_DATA_TYPE = "string";

  public void testConstructorDoesNotLeaveTheObjectInAnUnstableState() throws Exception {
    assertInvalidInputException(INT_DATA_TYPE, "xyz");
    assertInvalidInputException(INT_DATA_TYPE, "1.2");
    assertInvalidInputException(DOUBLE_DATA_TYPE, "1.2E");

  }

  private void assertInvalidInputException(String dataType, String value) {
    try {
      new Delta(dataType, value);
    } catch (InvalidInputException e) {
      assertEquals("value '" + value + "' is not a valid DataType = '" + dataType + "'", e.getMessage());
    }
  }

  public void testDeltaShouldEncapsulateTheValueAndDataType() throws Exception {
    Delta expectedDelta = new Delta(INT_DATA_TYPE, "5");
    assertEquals(expectedDelta, expectedDelta);
    assertFalse(expectedDelta.equals(null));
    assertEquals(expectedDelta, new Delta("int", "5"));
    assertEquals(expectedDelta, new Delta("Int", "5"));
    assertEquals(expectedDelta, new Delta("integer", "5"));
    assertEquals(expectedDelta, new Delta("IntegeR", "5"));
    expectedDelta = new Delta(DOUBLE_DATA_TYPE, "1.2");
    assertEquals(expectedDelta, new Delta("double", "1.20"));
    assertEquals(expectedDelta, new Delta("Double", "1.200"));
    expectedDelta = new Delta(STRING_DATA_TYPE, "value");
    assertEquals(expectedDelta, new Delta("String", "value"));
    assertEquals(new Delta(STRING_DATA_TYPE, "1.200"), new Delta("anyValueOtherThanIntAndDouble", "1.200"));
    Delta delta = new Delta("String", "ABC") {
      public String addTo(String originalValue, int numberofTime) {
        return null;
      }
    };
    assertFalse(expectedDelta.equals(delta));
  }

  public void testAdd() throws Exception {
    Delta int5 = new Delta(INT_DATA_TYPE, "5");
    assertEquals("10", int5.addTo("5", 1));
    assertEquals("20", int5.addTo("10", 2));
    assertEquals("0", int5.addTo("-5", 1));
    Delta double5Point2 = new Delta(DOUBLE_DATA_TYPE, "5.2");
    assertEquals("10.2", double5Point2.addTo("5", 1));
    assertEquals("10.402", double5Point2.addTo("0.002", 2));
    assertEquals("0.2", double5Point2.addTo("-5", 1));
    Delta stringABC = new Delta(STRING_DATA_TYPE, "ABC");
    assertEquals("5ABC", stringABC.addTo("5", 1));
    assertEquals("0.002ABC", stringABC.addTo("0.002", 1));
    assertEquals("-5ABCABCABC", stringABC.addTo("-5", 3));
    assertEquals("XYZABC", stringABC.addTo("XYZ", 1));
  }

  public void testToString() throws Exception {
    aasertToString("5.3", DOUBLE_DATA_TYPE);
    aasertToString("5.3", STRING_DATA_TYPE);
    aasertToString("5", INT_DATA_TYPE);
  }

  private void aasertToString(String value, String dataType) throws InvalidInputException {
    String expectedToStringValue = "DataType = '" + dataType + "' and value = " + value;
    assertEquals(expectedToStringValue, new Delta(dataType, value).toString());
  }
}
