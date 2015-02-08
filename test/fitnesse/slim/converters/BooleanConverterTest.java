package fitnesse.slim.converters;

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanConverterTest extends AbstractConverterTest<Boolean, BooleanConverter> {

  public BooleanConverterTest() {
    super(new BooleanConverter());
  }

  protected BooleanConverterTest(BooleanConverter booleanConverter) {
    super(booleanConverter);
  }

  /*
   * TO STRING
   */
  @Test
  public void toString_should_return_true_string_when_value_is_true() {
    Boolean value = Boolean.TRUE;

    String current = converter.toString(value);

    assertEquals(BooleanConverter.TRUE, current);
  }

  @Test
  public void toString_should_return_false_string_when_value_is_false() {
    Boolean value = Boolean.FALSE;

    String current = converter.toString(value);

    assertEquals(BooleanConverter.FALSE, current);
  }

  /*
   * FROM STRING
   */
  @Test
  public void fromString_should_return_true_when_value_is_true() {
    String trueValues[] = { "true", "yes", "YES", "TRUE" };

    for (String value : trueValues) {
      Boolean current = converter.fromString(value);

      assertTrue(current);
    }
  }

  @Test
  public void fromString_should_return_false_when_value_is_not_true() {
    String falseValues[] = { "false", "FALSE", "NO", "no", "x", "0" };

    for (String value : falseValues) {
      Boolean current = converter.fromString(value);

      assertFalse(current);
    }
  }
}
