package fitnesse.threadlocal;

import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalUtil {
  private static ThreadLocal<ConcurrentHashMap<String, Object>> _map = new ThreadLocal<ConcurrentHashMap<String, Object>>();

  private static ConcurrentHashMap<String, Object> map() {
    if (_map.get() == null)
      _map.set(new ConcurrentHashMap<String, Object>());
    return _map.get();
  }

  public static void setValue(String key, String value) {
    if (!map().containsKey(key)) {
      map().put(key, value);
      map().put(countKey(key), 1);
    } else {
      int currentValue = (Integer) (map().get(countKey(key)));
      map().put(countKey(key), currentValue + 1);
    }
  }

  public static String getValue(String key) {
    return (String) map().get(key);
  }

  public static String getValue(String key, String defaultValue) {
    if (!map().containsKey(key))
      return defaultValue;
    return getValue(key);
  }

  public static int getSetCount(String key) {
    return (Integer) (map().get(countKey(key)));
  }

  private static String countKey(String key) {
    return String.format("__%s:count__", key);
  }

  public static void clear() {
    _map.set(null);
  }
}
