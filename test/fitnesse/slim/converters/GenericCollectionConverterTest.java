package fitnesse.slim.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.slim.Converter;
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
  public void fromNull_shouldCreateNullString() {
    assertEquals(Converter.NULL_VALUE, converter.toString(null));
  }

  @Test
  public void toString_should_return_a_formated_string_when_value_is_a_empty_list() {
    List<Integer> value = new ArrayList<>();

    String current = converter.toString(value);

    assertEquals("[]", current);
  }

  public void toString_should_return_a_formated_string_when_value_is_a_valid_list() {
    List<Integer> value = new ArrayList<>();
    value.add(1);
    value.add(2);
    value.add(3);
    value.add(null);

    String current = converter.toString(value);

    assertEquals("[1, 2, 3, null]", current);
  }

  @Test
  public void toString_should_use_converters_for_element_values() {
    List<Object> value = Arrays.asList(1, Collections.singletonMap("a", "b"), 3, null);

    Converter c = new GenericCollectionConverter<>(ArrayList.class, new DefaultConverter());
    String current = c.toString(value);

    assertEquals("[1, <table class=\"hash_table\"> <tr class=\"hash_row\"> <td class=\"hash_key\">a</td> <td class=\"hash_value\">b</td> </tr> </table>, 3, null]",
            current.replaceAll("\\s+", " "));
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
  public void fromString_should_return_an_empty_list_when_value_represent_an_empty_collection() {
    String value = "[]";
    GenericCollectionConverter<Integer, Collection<Integer>> collConverter
            = new GenericCollectionConverter<>(Collection.class, new IntConverter());

    Collection<Integer> current = collConverter.fromString(value);

    assertEquals(0, current.size());
  }

  @Test
  public void fromString_should_return_an_typed_list_when_value_is_an_valid_list() {
    String value = "[1,2,3]";

    List<Integer> current = converter.fromString(value);

    assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), current);
  }

  @Test
  public void fromString_should_return_an_typed_list_when_value_is_an_valid_collection() {
    String value = "[1,2,3]";
    GenericCollectionConverter<Integer, Collection<Integer>> collConverter
            = new GenericCollectionConverter<>(Collection.class, new IntConverter());

    Collection<Integer> current = collConverter.fromString(value);

    assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), current);
  }

  @Test
  public void fromString_should_return_an_typed_list_with_null_value_when_value_is_an_list_with_null_values() {
    String value = "[1, ,3]";

    List<Integer> current = converter.fromString(value);

    assertEquals(Arrays.asList(new Integer[] { 1, null, 3 }), current);
  }
}
