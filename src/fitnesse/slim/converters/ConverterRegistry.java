package fitnesse.slim.converters;

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

  protected static void addStandardConverters() {
    addConverter(void.class, new VoidConverter());
    addConverter(String.class, new StringConverter());
    addConverter(int.class, new IntConverter());
    addConverter(double.class, new DoubleConverter());
    addConverter(Integer.class, new IntConverter());
    addConverter(Double.class, new DoubleConverter());
    addConverter(char.class, new CharConverter());
    addConverter(boolean.class, new BooleanConverter());
    addConverter(Boolean.class, new BooleanConverter());
    addConverter(Date.class, new DateConverter());
    addConverter(List.class, new ListConverter());
    addConverter(Integer[].class, new IntegerArrayConverter());
    addConverter(int[].class, new IntegerArrayConverter());
    addConverter(String[].class, new StringArrayConverter());
    addConverter(boolean[].class, new BooleanArrayConverter());
    addConverter(Boolean[].class, new BooleanArrayConverter());
    addConverter(double[].class, new DoubleArrayConverter());
    addConverter(Double[].class, new DoubleArrayConverter());
  }

  @SuppressWarnings("unchecked")
  public static <T> Converter<T> getConverterForClass(Class<? extends T> clazz) {
    return (Converter<T>) converters.get(clazz);
  }

  public static <T> void addConverter(Class<? extends T> clazz, Converter<T> converter) {
    converters.put(clazz, converter);
  }

  public static Map<Class<?>, Converter<?>> getConverters() {
    return Collections.unmodifiableMap(converters);
  }
}
