package fitnesse.slim;

import java.util.Map;
import java.util.HashMap;

public class Slim {
  static Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();

  public static void addConverter(Class<?> k, Converter converter) {
    converters.put(k, converter);
  }
}
