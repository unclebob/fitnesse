package fitnesse.slim.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class GenericCollectionConverterTest extends AbstractConverterTest<List<Integer>, GenericCollectionConverter<Integer, List<Integer>>> {

  public GenericCollectionConverterTest() {

    super(new GenericCollectionConverter<Integer, List<Integer>>(ArrayList.class, new IntConverter()));
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_a_formated_string_when_value_is_a_empty_list() {
    List<Integer> value = new ArrayList<Integer>();

    String current = converter.toString(value);

    assertEquals("[]", current);
  }

  public void toString_should_return_a_formated_string_when_value_is_a_valid_list() {
    List<Integer> value = new ArrayList<Integer>();
    value.add(1);
    value.add(2);
    value.add(3);

    String current = converter.toString(value);

    assertEquals("[1, 2, 3]", current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_an_empty_list_when_value_represent_an_empty_list() {
    String value = "[]";

    List<Integer> current = converter.fromString(value);

    assertEquals(0, current.size());
  }

  @Test
  public void fromString_should_return_an_typed_list_when_value_is_an_valid_list() {
    String value = "[1,2,3]";

    List<Integer> current = converter.fromString(value);

    assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), current);
  }

  @Test
  public void fromString_should_return_an_typed_list_with_null_value_when_value_is_an_list_with_null_values() {
    String value = "[1, ,3]";

    List<Integer> current = converter.fromString(value);

    assertEquals(Arrays.asList(new Integer[] { 1, null, 3 }), current);
  }
}
