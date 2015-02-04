package fitnesse.slim.converters;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrimitiveDoubleConverterTest extends DoubleConverterTest {
  public PrimitiveDoubleConverterTest() {
    super(new PrimitiveDoubleConverter());
  }

  @Override
  @Ignore
  //Add this method to remove this test from its parent 
  public void fromString_should_return_null_object_when_value_is_not_defined() {
  }

  @Test
  public void fromString_should_return_default_object_when_value_is_not_defined() {
    String value = "";

    Double current = converter.fromString(value);

    assertNotNull(current);
    assertEquals(PrimitiveDoubleConverter.DEFAULT_VALUE, current);
  }
}
