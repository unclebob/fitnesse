package fitnesse.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to obtain caches.
 */
public class CacheHelper {

  /**
   * Creates a new 'least recently used' cache. This means that when the cache becomes larger than the maximum
   * allowed size it will remove the elements that have not been accessed the longest.
   * @param maxSize maximum number of elements in the cache
   * @param <K> cache key type
   * @param <V> cached value type
   * @return 'least recently used' cache
   */
  public static <K, V> Map<K, V> lruCache(final int maxSize) {
    return new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
      }
    };
  }

}
