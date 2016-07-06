package fitnesse.slim.converters;

import java.util.Collections;

import fitnesse.slim.Converter;
import org.junit.Test;

import static org.junit.Assert.*;

public class GenericArrayConverterTest extends AbstractConverterTest<Object, GenericArrayConverter<Integer>> {

  public GenericArrayConverterTest() {
    super(new GenericArrayConverter<>(Integer.class, new IntConverter()));
  }

  /*
   * TO STRING
   */
  @Test
  public void fromNull_shouldCreateNullString() {
    assertEquals(Converter.NULL_VALUE, converter.toString(null));
  }

  @Test
  public void toString_should_return_a_formated_string_when_value_is_a_empty_array() {
    Integer[] value = {};

    String current = converter.toString(value);

    assertEquals("[]", current);
  }

  @Test
  public void toString_should_return_a_formated_string_when_value_is_a_valid_array() {
    Integer[] value = { 1, 2, 3, null };

    String current = converter.toString(value);

    assertEquals("[1, 2, 3, null]", current);
  }

  @Test
  public void toString_should_use_converters_for_element_values() {
    Object[] value = { 1, Collections.singletonMap("a", "b"), 3, null };

    Converter c = new GenericArrayConverter<>(Object.class, new DefaultConverter());
    String current = c.toString(value);

    assertEquals("[1, <table class=\"hash_table\"> <tr class=\"hash_row\"> <td class=\"hash_key\">a</td> <td class=\"hash_value\">b</td> </tr> </table>, 3, null]",
            current.replaceAll("\\s+", " "));
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_an_empty_array_when_value_represent_an_empty_array() {
    String value = "[]";

    Integer[] current = (Integer[]) converter.fromString(value);

    assertEquals(0, current.length);
  }

  @Test
  public void fromString_should_return_an_typed_array_when_value_is_an_valid_array() {
    String value = "[1,2,3]";

    Integer[] current = (Integer[]) converter.fromString(value);

    assertArrayEquals(new Integer[] { 1, 2, 3 }, current);
  }

  @Test
  public void fromString_should_return_an_typed_array_with_null_value_when_value_is_an_array_with_missing_values() {
    String value = "[1, ,3]";

    Integer[] current = (Integer[]) converter.fromString(value);

    assertArrayEquals(new Integer[] { 1, null, 3 }, current);
  }
}
