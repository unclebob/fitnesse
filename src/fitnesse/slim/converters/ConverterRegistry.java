package fitnesse.slim.converters;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fitnesse.slim.Converter;

public class ConverterRegistry {

  private static final Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();

  static {
    addStandardConverters();
  }

  protected static void addStandardConverters() {
    addConverter(void.class, new VoidConverter());

    addConverter(String.class, new StringConverter());
    addConverter(Date.class, new DateConverter());

    addConverter(Double.class, new DoubleConverter());
    addConverter(double.class, new PrimitiveDoubleConverter());

    addConverter(Integer.class, new IntConverter());
    addConverter(int.class, new PrimitiveIntConverter());

    addConverter(Character.class, new CharConverter());
    addConverter(char.class, new PrimitiveCharConverter());

    addConverter(Boolean.class, new BooleanConverter());
    addConverter(boolean.class, new PrimitiveBooleanConverter());

    try {
      addConverter(Map.class, new MapConverter());
    } catch (NoClassDefFoundError e) {
      System.err.println("Slim  Map converter not loaded: could not find class " + e.getMessage());
    }
  }

  public static <T> Converter<T> getConverterForClass(Class<? extends T> clazz) {
    Converter<T> converter = getConverterForClass(clazz, null);
    return converter;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Converter<T> getConverterForClass(Class<? extends T> clazz, ParameterizedType typedClazz) {

    //use converter set in registry
    if (converters.containsKey(clazz)) {
      return (Converter<T>) converters.get(clazz);
    }

    //use property editor
    PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
    if (pe != null && !"EnumEditor".equals(pe.getClass().getSimpleName())) {
      // com.sun.beans.EnumEditor and sun.beans.EnumEditor seem to be used in different usages.
      return new PropertyEditorConverter<T>(pe);
    }

    //for enum, use generic enum converter
    if (clazz.isEnum()) {
      return new GenericEnumConverter(clazz);
    }

    //for array, use generic array converter
    if (clazz.isArray()) {
      Class<?> componentType = clazz.getComponentType();
      Converter<?> converterForClass = getConverterForClassOrStringConverter(componentType);
      return new GenericArrayConverter(componentType, converterForClass);
    }

    //for collection, use generic collection converter
    if (Collection.class.isAssignableFrom(clazz)) {
      Class<?> componentType = typedClazz != null ? (Class<?>) typedClazz.getActualTypeArguments()[0] : String.class;
      Converter<?> converterForClass = getConverterForClassOrStringConverter(componentType);
      return new GenericCollectionConverter(clazz, converterForClass);
    }

    return null;
  }

  public static <T> void addConverter(Class<? extends T> clazz, Converter<T> converter) {
    converters.put(clazz, converter);
  }

  public static Map<Class<?>, Converter<?>> getConverters() {
    return Collections.unmodifiableMap(converters);
  }

  /*
   * PRIVATE
   */
  public static Converter<?> getConverterForClassOrStringConverter(Class<?> clazz) {
    Converter<?> converter = getConverterForClass(clazz);
    if (converter == null) {
      converter = getConverterForClass(String.class);
    }
    return converter;
  }
}
