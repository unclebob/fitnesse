package fitnesse.slim.converters;

import fitnesse.slim.SlimError;
import org.junit.Test;

import static org.junit.Assert.*;

public class GenericEnumConverterTest extends AbstractConverterTest<EnumToTest, GenericEnumConverter<EnumToTest>> {

  public GenericEnumConverterTest() {
    super(new GenericEnumConverter<>(EnumToTest.class));
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_an_enum_name_when_value_is_a_valid_enum() {
    EnumToTest value = EnumToTest.v_0;

    String current = converter.toString(value);

    assertEquals(value.name(), current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_the_char_when_value_is_a_char() {
    String value = "v_1";

    EnumToTest current = converter.fromString(value);

    assertEquals(EnumToTest.v_1, current);
  }

  @Test
  public void fromString_should_return_throw_Exception_when_value_is_not_a_int() {
    String errorMessage = "no error occurred";

    try {
      converter.fromString("fault");
    } catch (SlimError e) {
      errorMessage = e.getMessage();
    }
    assertEquals("message:<<Can't convert fault to enum value of type fitnesse.slim.converters.EnumToTest.>>", errorMessage);
  }
}

enum EnumToTest {
  v_0, v_1
}
