package fitnesse.util;

import fitnesse.ContextConfigurator;

/**
 * ClassUtils allows for class resolution (Class.forName) on a specially defined
 * class loader. In practice this will be the class loader created when the plugins
 * are loaded from <tt>plugins/</tt>.
 *
 * This class, however, exposes some a habit and I (Arjan) would love to get rid
 * of it again: the class loader value is kept in a <em>mutable</em>
 * static variable. This by itself is enough to desire to get rid of this class: it can mess up test runs in
 * unpredictable ways.
 *
 * Currently this class loader is set from {@link ContextConfigurator#makeFitNesseContext()} whenever a new FitNesseContext is
 * created. <tt>getClassLoader</tt>
 *
 * Note that there are still direct invocations of Class.forName in the code. Those pieces of code are executed on
 * the SUT or have otherwise no relation with the plugins system.
 */
@Deprecated
public class ClassUtils {

  private static ClassLoader classLoader;

  private ClassUtils() {
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  public static <T> Class<T> forName(String className) throws ClassNotFoundException {
    return (Class<T>) (classLoader == null ? Class.forName(className) : Class.forName(className, true, classLoader));
  }

  @Deprecated
  public static void setClassLoader(ClassLoader classLoader) {
    ClassUtils.classLoader = classLoader;
  }

  @Deprecated
  public static ClassLoader getClassLoader() {
    return classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
  }
}
