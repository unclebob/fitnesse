package fitnesse.util;

import util.Clock;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple Cache interface.
 */
public interface Cache<K, V> {

  V get(K key) throws Exception;

  void put(K key, V value);

  void evict(K key);

  public interface Loader<K, V> {
    V fetch(K key) throws Exception;
  }

  public interface ExpirationPolicy<K, V> {
    boolean isExpired(K key, V value, long lastModified);
  }

  /**
   * Builder for the Simple Cache.
   * @param <K> Key object type. Keys should be hashable.
   * @param <V> Value object type.
   */
  public class Builder<K, V> {
    private Loader<K, V> loader;
    private ExpirationPolicy<K, V> expirationPolicy = new NoExpirationPolicy<K, V>();

    public Builder<K, V> withLoader(Loader<K, V> loader) {
      this.loader = loader;
      return this;
    }

    public Builder<K, V> withExpirationPolicy(ExpirationPolicy<K, V> expirationPolicy) {
      this.expirationPolicy = expirationPolicy;
      return this;
    }

    public Cache<K, V> build() {
      return new Cache<K, V>() {
        private Map<K, SoftReference<CachedValue<V>>> cacheMap = new ConcurrentHashMap<K, SoftReference<CachedValue<V>>>();

        @Override
        public V get(K key) throws Exception {
          final SoftReference<CachedValue<V>> ref = cacheMap.get(key);
          final CachedValue<V> cachedValue = ref != null ? ref.get() : null;
          V value;
          if (cachedValue == null || expirationPolicy.isExpired(key, cachedValue.value, cachedValue.lastModified)) {
            value = loader.fetch(key);
            if (value != null) {
              put(key, value);
            } else {
              cacheMap.remove(key);
            }
          } else {
            value = cachedValue.value;
          }
          return value;
        }

        public void put(K key, V value) {
          CachedValue<V> cachedValue = new CachedValue<V>(value, Clock.currentTimeInMillis());
          cacheMap.put(key, new SoftReference<CachedValue<V>>(cachedValue));
        }

        @Override
        public void evict(K key) {
          cacheMap.remove(key);
        }


      };
    }

    private static class CachedValue<V> {
      private V value;
      private long lastModified;

      CachedValue(V value, long lastModified) {
        this.value = value;
        this.lastModified = lastModified;
      }
    }

    private static class NoExpirationPolicy<K, V> implements ExpirationPolicy<K, V> {

      @Override
      public boolean isExpired(K key, V value, long lastModified) {
        return false;
      }
    }

  }
}

