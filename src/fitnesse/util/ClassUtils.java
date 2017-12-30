package fitnesse.util;

public class ClassUtils {

  private static ClassLoader classLoader;

  private ClassUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> forName(String className) throws ClassNotFoundException {
    return (Class<T>) (classLoader == null ? Class.forName(className) : Class.forName(className, true, classLoader));
  }

  public static void setClassLoader(ClassLoader classLoader) {
    ClassUtils.classLoader = classLoader;
  }

  public static ClassLoader getClassLoader() {
    return classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
  }
}
