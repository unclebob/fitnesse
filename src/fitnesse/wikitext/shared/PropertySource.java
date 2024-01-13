package fitnesse.wikitext.shared;

import java.util.Optional;

public interface PropertySource {
  Optional<String> findProperty(String key);
  boolean hasProperty(String key);

  default String findProperty(String key, String defaultValue) {
    return findProperty(key).orElse(defaultValue);
  }
}
