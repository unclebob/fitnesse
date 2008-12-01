package fitnesse.slim;

import java.util.HashMap;
import java.util.Map;

public class Slim {
  static Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();

  public static void addConverter(Class<?> k, Converter converter) {
    converters.put(k, converter);
  }
}
