package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CachedInteraction extends DefaultInteraction {
  private static final class NotExisting { public NotExisting() {} }
  private static final Constructor<?> noConstructor = NotExisting.class.getConstructors()[0];

  private final Map<String, Constructor<?>> constructorsByClassAndArgs = new HashMap<>();
  private final Map<String, Class<?>> classCache = new HashMap<>();
  private final Map<MethodKey, Method> methodsByNameAndArgs = new HashMap<>();

  @Override
  protected Constructor<?> getConstructor(Class<?> clazz,
                                          Object[] args) {
    String key = String.format("%s_%d", clazz.getName(), args.length);
    Constructor<?> cached = constructorsByClassAndArgs.get(key);
    if (cached == noConstructor) return null;
    if (cached != null) return cached;

    Constructor<?> constructor = handleConstructorCacheMiss(clazz, args);
    if (constructor == null) {
      constructorsByClassAndArgs.put(key, noConstructor);
    } else {
      constructorsByClassAndArgs.put(key, constructor);
    }
    return constructor;
  }

  @Override
  protected Class<?> getClass(String className) {
    Class<?> k = classCache.get(className);
    if (k == NotExisting.class) return null;
    if (k != null) return k;

    k = handleClassCacheMiss(className);
    if (k == null) {
      classCache.put(className, NotExisting.class);
    } else {
      classCache.put(className, k);
    }
    return k;
  }

  private static class MethodKey {
    final String k;
    final String method;
    final int nArgs;

    public MethodKey(Class<?> k, String method, int nArgs) {
      this.k = k.getSimpleName();
      this.method = method;
      this.nArgs = nArgs;
    }

    public int hashCode() {
      return nArgs * 31 + method.hashCode() + 31 * k.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof MethodKey)) return false;
      MethodKey m = (MethodKey) o;
      if (!m.k.equals(k)) return false;
      if (m.nArgs != nArgs) return false;
      return m.method.equals(method);
    }
  }

  @Override
  protected Method findMatchingMethod(String methodName, Class<?> k, int nArgs) {
    MethodKey key = new MethodKey(k, methodName, nArgs);
    Method cached = this.methodsByNameAndArgs.get(key);
    if (cached != null) return cached;

    Method method = handleMethodCacheMiss(methodName, k, nArgs);

    this.methodsByNameAndArgs.put(key, method);
    return method;
  }

  protected Constructor<?> handleConstructorCacheMiss(Class<?> clazz, Object[] args) {
    return super.getConstructor(clazz, args);
  }

  protected Class<?> handleClassCacheMiss(String className) {
    return super.getClass(className);
  }

  protected Method handleMethodCacheMiss(String methodName, Class<?> k, int nArgs) {
    return super.findMatchingMethod(methodName, k, nArgs);
  }
}
