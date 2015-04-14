package fitnesse.slim;

import java.lang.reflect.Type;
import java.util.Date;

import org.junit.Test;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.StringConverter;
import static org.junit.Assert.*;

public class ConverterSupportTest {

  @Test
  public void convertArgs_should_return_null_when_value_is_null() {
    String value = null;
    Class<?> clazz = String.class;

    Object current = convertSingleValue(value, clazz);

    assertNull(current);
  }

  @Test
  public void convertArgs_should_return_the_same_object_when_value_is_instance_of_target_type() {
    Date value = new Date();
    Class<?> clazz = Date.class;

    Object current = convertSingleValue(value, clazz);

    assertSame(value, current);
  }


  @Test(expected = fitnesse.slim.SlimError.class)
  public void convertArgs_should_throw_an_exception_when_target_type_has_no_converter() {
    String value = "";
    Class<?> clazz = StringBuffer.class;

    convertSingleValue(value, clazz);

    fail();
  }

  @Test
  public void convertArgs_should_return_a_converted_object_when_converter_exists() {
    String value = "1";
    Class<?> clazz = Integer.class;

    Object current = convertSingleValue(value, clazz);

    assertEquals(Integer.valueOf(1), current);
  }

  @Test
  public void convertArgs_should_return_a_converted_object_when_value_is_a_string() {
    String value = "";
    Class<?> clazz = String.class;

    Object current = convertSingleValue(value, clazz);

    assertEquals(value, current);
  }

  @Test
  public void convertArgs_should_return_a_converted_object_when_converter_is_overridden() {
    Converter<String> stringConverter = ConverterRegistry.getConverterForClass(String.class);
    try {
      ConverterRegistry.addConverter(String.class, new MyStringConverter());

      Object current = convertSingleValue("input string", String.class);

      assertSame(MyStringConverter.CONVERTED_STRING, current);
    } finally {
      ConverterRegistry.addConverter(String.class, stringConverter);
    }
  }

  @Test
  public void should_throw_SlimError_if_value_cannot_be_converted() {
    String errorMessage = "no error";
    try {
      ConverterSupport.convertArgs(new Object[]{"val"}, new Type[]{ Runnable.class });
    } catch (SlimError e) {
      errorMessage = e.getMessage();
    }
    assertEquals("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER java.lang.Runnable.>>", errorMessage);
  }

  /*
   * PRIVATE
   */
  private static Object convertSingleValue(Object value, Class<?> type) {
    Object[] convertedArgs = ConverterSupport.convertArgs(new Object[] { value }, new Type[] { type });
    assertEquals(1, convertedArgs.length);
    return convertedArgs[0];
  }

  private static class MyStringConverter extends StringConverter {
    public static final String CONVERTED_STRING = "converted string";

    @Override
    public String fromString(String o) {
      return CONVERTED_STRING;
    }
  }
}
