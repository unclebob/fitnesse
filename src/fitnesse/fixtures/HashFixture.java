package fitnesse.fixtures;

import java.util.Map;
import java.util.TreeMap;

public class HashFixture {

  private Map hash;

  public void sendAsHash(Map hash) {
    this.hash = hash;
  }

  public Map hash() {
    // Make result predictable (ordered)
    return new TreeMap(hash);
  }

  public Object hashIs(String key) {
    return hash.get(key);
  }
}
