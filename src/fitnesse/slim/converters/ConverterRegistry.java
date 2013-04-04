package fitnesse.slim.converters;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.slim.Converter;

public class ConverterRegistry {

  static Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();

  static {
    addStandardConverters();
  }

  @SuppressWarnings("unchecked")
  protected static void addStandardConverters() {
    addConverter(void.class, new VoidConverter());
    addConverter(String.class, new StringConverter());
    addConverter(int.class, new IntConverter());
    addConverter(Integer.class, NullableConverter.decorate(new IntConverter()));
    addConverter(double.class, new DoubleConverter());
    addConverter(Double.class,
        NullableConverter.decorate(new DoubleConverter()));
    addConverter(char.class, new CharConverter());
    addConverter(Character.class,
        NullableConverter.decorate(new CharConverter()));
    addConverter(boolean.class, new BooleanConverter());
    addConverter(Boolean.class,
        NullableConverter.decorate(new BooleanConverter()));
    addConverter(Date.class, NullableConverter.decorate(new DateConverter()));
    addConverter(List.class, new ListConverter());
    addConverter(Integer[].class, new IntegerArrayConverter());
    addConverter(int[].class, new IntegerArrayConverter());
    addConverter(String[].class, new StringArrayConverter());
    addConverter(boolean[].class, new BooleanArrayConverter());
    addConverter(Boolean[].class, new BooleanArrayConverter());
    addConverter(double[].class, new DoubleArrayConverter());
    addConverter(Double[].class, new DoubleArrayConverter());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Converter<T> getConverterForClass(Class<? extends T> clazz) {
    if (converters.containsKey(clazz))
      return (Converter<T>) converters.get(clazz);

    PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
    // com.sun.beans.EnumEditor and sun.beans.EnumEditor seem to be used in
    // different usages.
    if (Enum.class.isAssignableFrom(clazz)
        && (pe == null || "EnumEditor".equals(pe.getClass().getSimpleName())))
      return new EnumConverter(clazz);

    if (pe != null) {
      return new PropertyEditorConverter<T>(pe);
    }
    return null;
  }

  public static <T> void addConverter(Class<? extends T> clazz,
      Converter<T> converter) {
    converters.put(clazz, converter);
  }

  public static Map<Class<?>, Converter<?>> getConverters() {
    return Collections.unmodifiableMap(converters);
  }
}
