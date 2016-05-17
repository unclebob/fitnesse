package fitnesse.slim.converters;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharConverterTest extends AbstractConverterTest<Character, CharConverter> {

  public CharConverterTest() {
    super(new CharConverter());
  }

  protected CharConverterTest(CharConverter charConverter) {
    super(charConverter);
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_string_which_contains_the_char_when_value_is_a_char() {
    Character value = Character.valueOf('a');

    String current = converter.toString(value);

    assertEquals(value.toString(), current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_the_char_when_value_is_a_char() {
    String value = "a";

    Character current = converter.fromString(value);

    assertEquals(Character.valueOf('a'), current);
  }
}
