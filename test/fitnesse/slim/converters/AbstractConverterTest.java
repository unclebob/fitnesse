package fitnesse.slim.converters;

import org.junit.Ignore;
import org.junit.Test;

import fitnesse.slim.Converter;
import static org.junit.Assert.*;

//tests run in children classes
@Ignore
public abstract class AbstractConverterTest<T, C extends Converter<T>> {
  protected final C converter;

  public AbstractConverterTest(C converter) {
    this.converter = converter;
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_null_string_when_value_is_not_defined() {
    T value = null;

    String current = converter.toString(value);

    assertEquals(Converter.NULL_VALUE, current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_null_object_when_value_is_not_defined() {
    String value = "";

    T current = converter.fromString(value);

    assertNull(current);
  }
}
