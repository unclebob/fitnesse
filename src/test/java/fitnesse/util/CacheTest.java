package fitnesse.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class CacheTest {

  @Test
  public void shouldBuildCache() throws Exception {
    Cache<String, Integer> cache = new Cache.Builder<String, Integer>()
            .withLoader(new Cache.Loader<String, Integer>() {

              @Override
              public Integer fetch(String key) {
                return key.length();
              }
            })
            .build();
    assertEquals(3, cache.get("str").intValue());
  }

  @Test
  public void shouldHoldCachedData() throws Exception {
    Cache<String, Dummy> cache = new Cache.Builder<String, Dummy>()
            .withLoader(new Cache.Loader<String, Dummy>() {

              @Override
              public Dummy fetch(String key) {
                return new Dummy(key);
              }
            })
            .build();

    Dummy value = cache.get("str");
    Dummy anotherValue = cache.get("str2");

    assertTrue(value != anotherValue);
    assertTrue(value == cache.get("str"));
    assertTrue(anotherValue == cache.get("str2"));
  }

  @Test
  public void shouldEvictCachedData() throws Exception {
    Cache<String, Dummy> cache = new Cache.Builder<String, Dummy>()
            .withLoader(new Cache.Loader<String, Dummy>() {
              @Override public Dummy fetch(String key) {
                return new Dummy(key);
              }
            })
            .withExpirationPolicy(new Cache.ExpirationPolicy<String, Dummy>() {
              @Override public boolean isExpired(String key, Dummy value, long lastModified) {
                  return key.length() == 3;
              }
            })
            .build();

    Dummy value = cache.get("str");
    Dummy anotherValue = cache.get("str2");

    assertTrue(value != cache.get("str"));
    assertTrue(anotherValue == cache.get("str2"));
  }

  @Test
  public void shouldNotCacheIfLoadedValueIsNull() throws Exception {
    final List<Integer> calls = new ArrayList<Integer>();

    Cache<String, Dummy> cache = new Cache.Builder<String, Dummy>()
            .withLoader(new Cache.Loader<String, Dummy>() {
              @Override public Dummy fetch(String key) {
                calls.add(1);
                return null;
              }
            })
            .build();

    cache.get("s");
    assertEquals(1, calls.size());
    cache.get("s");
    assertEquals(2, calls.size());

  }

  public static class Dummy {
    final String data;
    Dummy(String data) {
      this.data = data;
    }
  }
}
