package fit.decorator.util;

import java.awt.Point;

import junit.framework.TestCase;
import fit.decorator.exceptions.InvalidInputException;

public class DataTypeTest extends TestCase {
  public void testInstanceMethodReturnsValidDataTypeForGivenParam() throws Exception {
    assertSame(DataType.INTEGER, DataType.instance("int"));
    assertSame(DataType.INTEGER, DataType.instance("Int"));
    assertSame(DataType.INTEGER, DataType.instance("integer"));
    assertSame(DataType.INTEGER, DataType.instance("INTEGER"));
    assertSame(DataType.DOUBLE, DataType.instance("double"));
    assertSame(DataType.DOUBLE, DataType.instance("DOUBLE"));
    assertSame(DataType.STRING, DataType.instance("string"));
    assertSame(DataType.STRING, DataType.instance("STRING"));
    assertSame(DataType.DATE, DataType.instance("DATE"));
  }

  public void testInstanceMethodDefaultsDataTypeToSTRINGIfGivenParamIsInvalid() throws Exception {
    assertSame(DataType.STRING, DataType.instance("invalid"));
  }

  public void testParseMethodParsesInputStringAndConvertsItToAppropriateObject() throws Exception {
    Object parsedObject = DataType.INTEGER.parse("10");
    assertEquals(10, ((Integer) parsedObject).intValue());

    parsedObject = DataType.DOUBLE.parse("10.3");
    assertEquals(10.3, ((Double) parsedObject).doubleValue(), 0.01);

    parsedObject = DataType.STRING.parse("Some String");
    assertEquals("Some String", ((String) parsedObject));

    parsedObject = DataType.DATE.parse("10");
    assertEquals(10, ((Integer) parsedObject).intValue());
  }

  public void testParseMethodThrowsInvalidInputExceptionIfInputStringIsCannotBeParsed() throws Exception {
    assertInvalidInputException("Invalid Integer", DataType.INTEGER, "int");
    assertInvalidInputException("1.2", DataType.INTEGER, "int");
    assertInvalidInputException("Invalid Double", DataType.DOUBLE, "double");
    assertInvalidInputException("1.2E", DataType.DOUBLE, "double");
    assertInvalidInputException("12-2-2006", DataType.DATE, "date");
  }

  private void assertInvalidInputException(String value, DataType dataType, String dataTypeString) {
    try {
      dataType.parse(value);
      fail("Should have thrown InvalidInputException since '" + value + "' is not a valid " + dataTypeString);
    } catch (InvalidInputException e) {
      assertEquals("value '" + value + "' is not a valid DataType = '" + dataTypeString + "'", e.getMessage());
    }
  }

  public void testAddToMethodAddsGivenValueGivenNumberOfTimesToOriginalValue() throws Exception {
    assertEquals("10", DataType.INTEGER.addTo("10", new Integer(5), 0));
    assertEquals("15", DataType.INTEGER.addTo("10", new Integer(5), 1));
    assertEquals("20", DataType.INTEGER.addTo("10", new Integer(5), 2));
    assertEquals("10.2", DataType.DOUBLE.addTo("10.2", new Double(5.1), 0));
    assertEquals("15.3", DataType.DOUBLE.addTo("10.2", new Double(5.1), 1));
    assertEquals("20.4", DataType.DOUBLE.addTo("10.2", new Double(5.1), 2));
    assertEquals("Hello", DataType.STRING.addTo("Hello", " World", 0));
    assertEquals("Hello World", DataType.STRING.addTo("Hello", " World", 1));
    assertEquals("Hello World World", DataType.STRING.addTo("Hello", " World", 2));
    assertEquals("12/02/2006", DataType.DATE.addTo("12/02/2006", new Integer(5), 0));
    assertEquals("12/07/2006", DataType.DATE.addTo("12/02/2006", new Integer(5), 1));
    assertEquals("01/01/2007", DataType.DATE.addTo("12/31/2006", new Integer(1), 1));
    assertEquals("01/21/2007", DataType.DATE.addTo("12/02/2006", new Integer(5), 10));
  }

  public void testShouldBeAbleToAddCustomDataTypes() throws Exception {
    DataType.registerUserDefinedDataTypes(Point.class.getName(), new PointDataType());
    DataType returnedDataType = DataType.instance(Point.class.getName());
    assertEquals("(5,5)", returnedDataType.addTo("(5,5)", new Point(1, 1), 0));
    assertEquals("(6,6)", returnedDataType.addTo("(5,5)", new Point(1, 1), 1));
    assertEquals("(10,10)", returnedDataType.addTo("(5,5)", new Point(1, 1), 5));
  }

  public void testShouldBeAbleToRemoveSpecificCustomDataTypes() throws Exception {
    DataType.registerUserDefinedDataTypes(Point.class.getName(), new PointDataType());
    DataType returnedDataType = DataType.instance(Point.class.getName());
    assertEquals("(6,6)", returnedDataType.addTo("(5,5)", new Point(1, 1), 1));
    DataType.clearUserDefinedDataTypes(Point.class.getName());
    returnedDataType = DataType.instance(Point.class.getName());
    assertEquals("(5,5)java.awt.Point[x=1,y=1]", returnedDataType.addTo("(5,5)", new Point(1, 1), 1));
  }

  public void testShouldBeAbleToRemoveAllCustomDataTypes() throws Exception {
    DataType.registerUserDefinedDataTypes(Point.class.getName(), new PointDataType());
    DataType returnedDataType = DataType.instance(Point.class.getName());
    assertEquals("(6,6)", returnedDataType.addTo("(5,5)", new Point(1, 1), 1));
    DataType.clearUserDefinedDataTypes();
    returnedDataType = DataType.instance(Point.class.getName());
    assertEquals("(5,5)java.awt.Point[x=1,y=1]", returnedDataType.addTo("(5,5)", new Point(1, 1), 1));
  }
}
