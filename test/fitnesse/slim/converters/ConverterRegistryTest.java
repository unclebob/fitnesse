package fitnesse.slim.converters;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import org.junit.Test;

import fitnesse.slim.Converter;
import fitnesse.slim.test.AnEnum;
import fitnesse.slim.test.AnotherEnum;
import static org.junit.Assert.*;

public class ConverterRegistryTest {

  @Test
  public void getConverters_should_return_several_default_converter() {
    Map<Class<?>, Converter<?>> current = ConverterRegistry.getConverters();

    assertTrue(current.size() > 0);
    assertNotNull(current.get(Integer.class));
    assertNotNull(current.get(Double.class));
    assertNotNull(current.get(Date.class));
  }

  @Test
  public void getConverterForClass_should_return_a_custom_converter_when_a_custom_converter_has_been_set() {
    ConverterRegistry.addConverter(StringBuilder.class, new StringBuilderConverter());

    Converter<StringBuilder> converter = ConverterRegistry.getConverterForClass(StringBuilder.class);
    String current = converter.toString(new StringBuilder());

    assertTrue(StringBuilderConverter.class.isInstance(converter));
    assertEquals("customConverter", current);
  }

  @Test
  public void getConverterForClass_should_return_a_property_editor_converter_when_editor_exists() throws Exception {
    Class<AnotherEnum> value = AnotherEnum.class;

    Converter<AnotherEnum> actual = ConverterRegistry.getConverterForClass(value);

    assertTrue(PropertyEditorConverter.class.isInstance(actual));
    assertConverts("enum property editor called with \"some value\"", actual, "some value");
  }

  private <T> void assertConverts(String expected, Converter<T> converter, String value) {
    assertEquals(expected, converter.toString(converter.fromString(value)));
  }

  @Test
  public void getConverterForClass_should_return_a_enum_converter_when_value_is_an_enum() {
    Class<AnEnum> value = AnEnum.class;

    Converter<AnEnum> actual = ConverterRegistry.getConverterForClass(value);

    assertTrue(GenericEnumConverter.class.isInstance(actual));
  }

  @Test
  public void getConverterForClass_should_return_an_array_converter_when_value_is_an_array_of_primitive() {
    Class<int[]> value = int[].class;

    Converter<int[]> current = ConverterRegistry.getConverterForClass(value);
    int[] value2 = current.fromString("[1]");

    assertTrue(current instanceof GenericArrayConverter);
    assertEquals(1, value2[0]);
  }

  @Test
  public void getConverterForClass_should_return_an_array_converter_when_value_is_an_array() {
    Class<Integer[]> clazz = Integer[].class;

    Converter<Integer[]> current = ConverterRegistry.getConverterForClass(clazz);
    Integer[] value2 = current.fromString("[1]");

    assertTrue(current instanceof GenericArrayConverter);
    assertEquals(Integer.valueOf(1), value2[0]);
  }

  @Test
  public void getConverterForClass_should_return_a_MapConverter_when_type_is_typed_map() throws SecurityException, NoSuchMethodException {
    Class<?> typedClass = MyFixture.class.getMethod("getMap").getReturnType();

    Converter<?> converter = ConverterRegistry.getConverterForClass(typedClass);

    assertNotNull("no converter retunred", converter);
    assertEquals(MapConverter.class, converter.getClass());
  }

  @Test
  public void getConverterForClass_should_return_a_MapConverter_when_type_implements_map() throws SecurityException, NoSuchMethodException {
    Class<?> typedClass = MyFixture.class.getMethod("getLinkedMap").getReturnType();

    Converter<?> converter = ConverterRegistry.getConverterForClass(typedClass);

    assertNotNull("no converter retunred", converter);
    assertEquals(MapConverter.class, converter.getClass());
  }

  @Test
  public void getConverterForClass_should_return_Converter_when_type_superclass_registered() throws SecurityException, NoSuchMethodException {
    ConverterRegistry.addConverter(MyFixture.class, new MyFixtureConverter());

    Converter<?> converter = ConverterRegistry.getConverterForClass(MySubFixture.class);

    assertNotNull("no converter retunred", converter);
    assertEquals(MyFixtureConverter.class, converter.getClass());
  }

  private static class MyFixture {
    public Map<String, Object> getMap() {
      return null;
    }
    public LinkedHashMap<String, Object> getLinkedMap() {
      return null;
    }
  }

  private static class MyFixtureConverter implements Converter<MyFixture> {
    @Override
    public String toString(MyFixture o) {
      return null;
    }
    @Override
    public MyFixture fromString(String arg) {
      return null;
    }
  }

  private static class MySubFixture extends MyFixture {
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void getConverterForClass_should_return_a_collection_of_string_when_value_is_a_list() {
    Class<List> clazz = List.class;

    Converter<List> converter = ConverterRegistry.getConverterForClass(clazz);
    Object current = converter.fromString("[1]").get(0);

    assertTrue(converter instanceof GenericCollectionConverter);
    assertTrue(current instanceof String);
    assertEquals("1", current);
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void getConverterForClass_should_return_a_collection_of_typed_object_with_converter_when_value_is_a_typed_list() throws SecurityException, NoSuchMethodException {
    Class<List> clazz = List.class;

    ParameterizedType typedClass = (ParameterizedType) new ArrayList<Integer>() {} .getClass().getGenericSuperclass();

    Converter<List> converter = ConverterRegistry.getConverterForClass(clazz, typedClass);
    Object current = converter.fromString("[1]").get(0);

    assertTrue(converter instanceof GenericCollectionConverter);
    assertTrue(current instanceof Integer);
    assertEquals(Integer.valueOf(1), current);
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void getConverterForClass_should_return_a_collection_of_typed_object_without_converter_when_value_is_a_typed_list() throws SecurityException, NoSuchMethodException {
    Class<List> clazz = List.class;

    ParameterizedType typedClass = (ParameterizedType) new ArrayList<Object>() {} .getClass().getGenericSuperclass();

    Converter<List> converter = ConverterRegistry.getConverterForClass(clazz, typedClass);
    Object current = converter.fromString("[1]").get(0);

    assertTrue(converter instanceof GenericCollectionConverter);
    assertTrue(current instanceof String);
    assertEquals("1", current);
  }

  /*
   * PRIVATE
   */
  private static class StringBuilderConverter implements Converter<StringBuilder> {
    public String toString(StringBuilder o) {
      return "customConverter";
    }

    public StringBuilder fromString(String arg) {
      throw new IllegalStateException();
    }
  }

}
