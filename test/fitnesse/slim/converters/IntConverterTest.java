package fitnesse.slim.converters;

import fitnesse.slim.SlimError;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntConverterTest extends AbstractConverterTest<Integer, IntConverter> {

  public IntConverterTest() {
    super(new IntConverter());
  }

  protected IntConverterTest(IntConverter intConverter) {
    super(intConverter);
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_string_which_contains_the_int_when_value_is_a_int() {
    Integer value = 31415962;

    String current = converter.toString(value);

    assertEquals(value.toString(), current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_the_int_when_value_is_a_int() {
    String value = "31415962";

    Integer current = converter.fromString(value);

    assertEquals(Integer.valueOf(value), current);
  }

  @Test
  public void fromString_should_return_throw_Exception_when_value_is_not_a_int() {
    String value = "foo";
    String errorMessage = "no error";

    try {
      converter.fromString(value);
    } catch (SlimError e) {
      errorMessage = e.getMessage();
    }
    assertEquals("message:<<Can't convert foo to integer.>>", errorMessage);
  }

}
