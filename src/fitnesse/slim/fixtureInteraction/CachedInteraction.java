package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CachedInteraction extends DefaultInteraction {
  private static final Constructor<?> noConstructor = NotExisting.class.getConstructors()[0];
  private static final Method noMethod = NotExisting.class.getDeclaredMethods()[0];

  private final Map<String, Constructor<?>> constructorsByClassAndArgs = new HashMap<>();
  private final Map<String, Class<?>> classCache = new HashMap<>();
  private final Map<MethodKey, Method> methodsByNameAndArgs = new HashMap<>();

  @Override
  protected Constructor<?> getConstructor(Class<?> clazz, Object[] args) {
    String key = getConstructorKey(clazz, args);
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

  protected String getConstructorKey(Class<?> clazz, Object[] args) {
    return clazz.getName()+ "_" + args.length;
  }

  @Override
  protected Class<?> getClass(String className) {
    Class<?> cached = classCache.get(className);
    if (cached == NotExisting.class) return null;
    if (cached != null) return cached;

    Class<?> k = handleClassCacheMiss(className);
    if (k == null) {
      classCache.put(className, NotExisting.class);
    } else {
      classCache.put(className, k);
    }
    return k;
  }

  @Override
  protected Method findMatchingMethod(String methodName, Object instance, Object... args) {
    MethodKey key = new MethodKey(instance.getClass(), methodName, args.length);
    Method cached = methodsByNameAndArgs.get(key);
    if (cached == noMethod) return null;
    if (cached != null) return cached;

    Method method = handleMethodCacheMiss(methodName, instance, args);

    if (method == null) {
      methodsByNameAndArgs.put(key, noMethod);
    } else {
      methodsByNameAndArgs.put(key, method);
    }
    return method;
  }

  protected Constructor<?> handleConstructorCacheMiss(Class<?> clazz, Object[] args) {
    return super.getConstructor(clazz, args);
  }

  protected Class<?> handleClassCacheMiss(String className) {
    return super.getClass(className);
  }

  protected Method handleMethodCacheMiss(String methodName, Object instance, Object[] args) {
    return super.findMatchingMethod(methodName, instance, args);
  }

  private static final class MethodKey {
    private final String k;
    private final String method;
    private final int nArgs;

    public MethodKey(Class<?> k, String method, int nArgs) {
      this.k = k.getName();
      this.method = method;
      this.nArgs = nArgs;
    }

    @Override
    public int hashCode() {
      int result = k.hashCode();
      result = 31 * result + method.hashCode();
      result = 31 * result + nArgs;
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;

      MethodKey methodKey = (MethodKey) o;

      if (nArgs != methodKey.nArgs) return false;
      if (!k.equals(methodKey.k)) return false;
      return method.equals(methodKey.method);
    }
  }

  private static final class NotExisting {
    public NotExisting() {
    }

    public void doIt() {
    }
  }
}
