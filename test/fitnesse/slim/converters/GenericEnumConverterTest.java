package fitnesse.slim.converters;

import org.junit.Test;

import static org.junit.Assert.*;

public class GenericEnumConverterTest extends AbstractConverterTest<EnumToTest, GenericEnumConverter<EnumToTest>> {

  public GenericEnumConverterTest() {
    super(new GenericEnumConverter<EnumToTest>(EnumToTest.class));
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
}

enum EnumToTest {
  v_0, v_1
}
