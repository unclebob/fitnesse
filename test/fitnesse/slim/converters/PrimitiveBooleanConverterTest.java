package fitnesse.slim.converters;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrimitiveBooleanConverterTest extends BooleanConverterTest {

  public PrimitiveBooleanConverterTest() {
    super(new PrimitiveBooleanConverter());
  }

  @Override
  @Ignore
  //Add this method to remove this test from its parent 
  public void fromString_should_return_null_object_when_value_is_not_defined() {
  }

  @Test
  public void fromString_should_return_false_object_when_value_is_not_defined() {
    String value = "";

    Boolean current = converter.fromString(value);

    assertNotNull(current);
    assertEquals(PrimitiveBooleanConverter.DEFAULT_VALUE, current);
  }
}
