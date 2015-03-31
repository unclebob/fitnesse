package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringConverterTest {

  private final StringConverter converter;

  public StringConverterTest() {
    converter = new StringConverter();
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_the_same_string_when_value_is_a_string() {
    String value = "^_^";

    String current = converter.toString(value);

    assertSame(value, current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void toString_should_return_null_string_when_value_is_not_defined() {
    String value = null;

    String current = converter.toString(value);

    assertEquals(Converter.NULL_VALUE, current);
  }

  @Test
  public void fromString_should_return_the_same_string_when_value_is_a_string() {
    String value = "^_^";

    String current = converter.fromString(value);

    assertSame(value, current);
  }

  @Test
  public void fromString_should_return_empty_string() {
    String value = "";

    String current = converter.fromString(value);

    assertSame(value, current);
  }

}
