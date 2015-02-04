package fitnesse.slim.converters;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleConverterTest extends AbstractConverterTest<Double, DoubleConverter> {

  public DoubleConverterTest() {
    super(new DoubleConverter());
  }

  protected DoubleConverterTest(DoubleConverter doubleConverter) {
    super(doubleConverter);
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_string_which_contains_the_double_when_value_is_a_double() {
    Double value = 1.7320508;

    String current = converter.toString(value);

    assertEquals(value.toString(), current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_the_double_when_value_is_a_double() {
    String value = "1.732050807568877";

    Double current = converter.fromString(value);

    assertEquals(Double.valueOf(value), current);
  }
}
