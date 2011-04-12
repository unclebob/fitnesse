package fitnesse.slim.converters;

import fitnesse.slim.Converter;

import java.util.*;

public class ConverterRegistry {

  static Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();

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

  public static Converter getConverterForClass(Class<?> clazz) {
    return converters.get(clazz);
  }

  public static void addConverter(Class<?> clazz, Converter converter) {
    converters.put(clazz, converter);
  }

  public static Map<Class<?>, Converter> getConverters() {
    return Collections.unmodifiableMap(converters);
  }
}
