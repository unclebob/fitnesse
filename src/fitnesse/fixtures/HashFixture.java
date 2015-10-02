package fitnesse.fixtures;

import java.util.Map;
import java.util.TreeMap;

public class HashFixture {

  private Map<String, Object> hash;

  public void sendAsHash(Map<String, Object> hash) {
    this.hash = hash;
  }

  public Map<String, Object> hash() {
    // Make result predictable (ordered)
    return new TreeMap<String, Object>(hash);
  }

  public Object hashIs(String key) {
    return hash.get(key);
  }
}
