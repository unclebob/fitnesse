package fitnesse.util;

public class ClassUtils {

  private ClassUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> forName(String className) throws ClassNotFoundException {
    return (Class<T>) Class.forName(className);
  }

}
