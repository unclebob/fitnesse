package fitnesse.slim.converters;

import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.Converter;
import org.junit.Test;

import static org.junit.Assert.*;

public class ListConverterHelperTest {

  /*
   * TO STRING
   */
  @Test
  public void fromNull_shouldCreateNullString() {
    assertEquals(Converter.NULL_VALUE, ListConverterHelper.toString(null));
  }

  @Test
  public void toString_should_return_string_represents_empy_list_when_list_is_empty() throws Exception {
    List<String> value = new ArrayList<>();

    String current = ListConverterHelper.toString(value);

    assertEquals("[]", current);
  }

  @Test
  public void toString_should_return_values_when_list_is_valid() throws Exception {
    List<Integer> value = new ArrayList<>();
    value.add(1);
    value.add(2);

    String current = ListConverterHelper.toString(value);

    assertEquals("[1, 2]", current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_empty_list_when_entry_is_empty() throws Exception {
    String[] values = { "[]", "", " " };

    for (String value : values) {
      String[] current = ListConverterHelper.fromStringToArrayOfStrings(value);

      assertEquals(0, current.length);
    }
  }

  @Test
  public void fromString_should_return_list_when_entry_contains_items_with_brakets() throws Exception {
    String value = "[1,2,3]";

    String[] current = ListConverterHelper.fromStringToArrayOfStrings(value);

    assertEquals(3, current.length);
    assertArrayEquals(new String[] { "1", "2", "3" }, current);
  }

  @Test
  public void fromString_should_return_list_when_entry_contains_items_without_brakets() throws Exception {
    String value = "1,2,3";

    String[] current = ListConverterHelper.fromStringToArrayOfStrings(value);

    assertEquals(3, current.length);
    assertArrayEquals(new String[] { "1", "2", "3" }, current);
  }

  @Test
  public void fromString_should_return_list_when_entry_contains_items_with_spaces() throws Exception {
    String value = "1,  2,  3  ";

    String[] current = ListConverterHelper.fromStringToArrayOfStrings(value);

    assertEquals(3, current.length);
    assertArrayEquals(new String[] { "1", "2", "3" }, current);
  }

}
