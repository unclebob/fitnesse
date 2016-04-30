package fitnesse.fixtures;

import java.util.Map;
import java.util.TreeMap;

public class HashFixture {

  private Map<String, String> hash;

  public void sendAsHash(Map<String, String> hash) {
    this.hash = hash;
  }

  public Map<String, String> hash() {
    // Make result predictable (ordered)
    return new TreeMap<>(hash);
  }

  public Object hashIs(String key) {
    System.out.println(String.format("Hash %s is %s", key, hash.get(key)));
    return hash.get(key);
  }
}
