package fitnesse.slim.converters;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import fitnesse.slim.Converter;

public class ConverterRegistry {

  private static final Map<Class<?>, Converter<?>> converters = new HashMap<>();
  private static Converter<Object> defaultConverter = new DefaultConverter();

  static {
    addStandardConverters();
  }

  private ConverterRegistry() {
  }

  public static void resetToStandardConverters() {
    converters.clear();
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

  public static <T> Converter<T> getConverterForClass(Class<T> clazz) {
    return getConverterForClass(clazz, null);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Converter<T> getConverterForClass(Class<? extends T> clazz, ParameterizedType typedClazz) {

    //use converter set in registry
    if (converters.containsKey(clazz)) {
      return (Converter<T>) converters.get(clazz);
    }
    // use converter for superclass set in registry
    Class<?> superclass = clazz.getSuperclass();
    while (superclass != null && !Object.class.equals(superclass)) {
      if (converters.containsKey(superclass)) {
        return (Converter<T>) converters.get(superclass);
      }
      superclass = superclass.getSuperclass();
    }
    // use converter for implemented interface set in registry
    Converter<T> converterForInterface = getConverterForInterface(clazz);
    if (converterForInterface != null) {
      return converterForInterface;
    }

    //use property editor
    PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
    if (pe != null && !"EnumEditor".equals(pe.getClass().getSimpleName())) {
      // com.sun.beans.EnumEditor and sun.beans.EnumEditor seem to be used in different usages.
      return new PropertyEditorConverter<>(pe);
    }

    //for enum, use generic enum converter
    if (clazz.isEnum()) {
      return new GenericEnumConverter(clazz);
    }

    //for array, use generic array converter
    if (clazz.isArray()) {
      Class<?> componentType = clazz.getComponentType();
      Converter<?> converterForClass = getConverterForClassOrDefaultConverter(componentType);
      return new GenericArrayConverter(componentType, converterForClass);
    }

    //for collection, use generic collection converter
    if (Collection.class.isAssignableFrom(clazz)) {
      Class<?> componentType = typedClazz != null ? (Class<?>) typedClazz.getActualTypeArguments()[0] : String.class;
      Converter<?> converterForClass = getConverterForClassOrDefaultConverter(componentType);
      return new GenericCollectionConverter(clazz, converterForClass);
    }

    // last resort, see if there is a converter for Object
    return (Converter<T>) converters.get(Object.class);
  }

  protected static <T> Converter<T> getConverterForInterface(Class<?> clazz) {
    List<Class<?>> superInterfaces = new ArrayList<>();
    Converter<T> converterForInterface = null;
    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> interf : interfaces) {
      Class<?>[] s = interf.getInterfaces();
      superInterfaces.addAll(Arrays.asList(s));
      if (converters.containsKey(interf)) {
        converterForInterface = (Converter<T>) converters.get(interf);
        break;
      }
    }
    if (converterForInterface == null) {
      for (Class<?> supInterf : superInterfaces) {
        converterForInterface = getConverterForInterface(supInterf);
        if (converterForInterface != null) {
          break;
        }
      }

      if (converterForInterface == null) {
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && !Object.class.equals(superclass)) {
          converterForInterface = getConverterForInterface(superclass);
          if (converterForInterface != null) {
            break;
          }
          superclass = superclass.getSuperclass();
        }

      }
    }
    return converterForInterface;
  }

  public static <T> void addConverter(Class<? extends T> clazz, Converter<T> converter) {
    converters.put(clazz, converter);
  }

  public static void removeConverter(Class<?> clazz) {
    converters.remove(clazz);
  }

  public static Map<Class<?>, Converter<?>> getConverters() {
    return Collections.unmodifiableMap(converters);
  }

  /*
   * PRIVATE
   */
  public static Converter<?> getConverterForClassOrDefaultConverter(Class<?> clazz) {
    Converter<?> converter = getConverterForClass(clazz);
    if (converter == null) {
      converter = defaultConverter;
    }
    return converter;
  }
}
