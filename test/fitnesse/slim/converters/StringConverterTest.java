package fitnesse.slim.converters;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringConverterTest extends AbstractConverterTest<String, StringConverter> {

  public StringConverterTest() {
    super(new StringConverter());
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
  public void fromString_should_return_the_same_string_when_value_is_a_string() {
    String value = "^_^";

    String current = converter.fromString(value);

    assertSame(value, current);
  }

}
