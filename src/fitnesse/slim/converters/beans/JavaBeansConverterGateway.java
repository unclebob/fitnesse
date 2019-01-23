package fitnesse.slim.converters.beans;

import fitnesse.slim.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Gateway providing converters based on Java Beans property editors.
 * It uses reflection to call code which relies on the java.beans package,
 * which is not available on Android.
 * On Android these converters will not usable, i.e. #getConverter() will always return
 * <code>null</code>.
 */
public class JavaBeansConverterGateway {
  private static final Method javaBeansGetConverterMethod;

  static {
    Method getConverterMethod = null;
    // java.beans is not available on Android, so we cannot use JavaBeansPropertyEditorConverterFactory
    // directly but only via reflection
    try {
      Class<?> factory = Class.forName("fitnesse.slim.converters.beans.PropertyEditorConverterFactory");
      getConverterMethod = factory.getMethod("getConverter", Class.class);
    } catch (ClassNotFoundException e) {
    } catch (NoSuchMethodException e) {
    }
    javaBeansGetConverterMethod = getConverterMethod;
  }

  private JavaBeansConverterGateway() {
  }

  @SuppressWarnings("unchecked")
  public static <T> Converter<T> getConverter(Class<? extends T> clazz) {
    if (javaBeansGetConverterMethod != null) {
      try {
        return (Converter<T>) javaBeansGetConverterMethod.invoke(null, clazz);
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      }
    }
    return null;
  }
}
