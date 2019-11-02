package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LongConverterTest extends AbstractConverterTest<Long, LongConverter> {

  public LongConverterTest() {
    super(new LongConverter());
  }

  protected LongConverterTest(LongConverter longConverterConverter) {
    super(longConverterConverter);
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_string_which_contains_the_long_when_value_is_a_long() {
    Long value = 31415962l;

    String current = converter.toString(value);

    assertEquals(value.toString(), current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_the_long_when_value_is_a_long() {
    String value = "31415962";

    Long current = converter.fromString(value);

    assertEquals(Long.valueOf(value), current);
  }

  @Test
  public void fromString_should_return_throw_Exception_when_value_is_not_a_long() {
    String value = "foo";
    String errorMessage = "no error";

    try {
      converter.fromString(value);
    } catch (SlimError e) {
      errorMessage = e.getMessage();
    }
    assertEquals("message:<<Can't convert foo to long.>>", errorMessage);
  }

}
