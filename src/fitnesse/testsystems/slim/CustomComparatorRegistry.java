package fitnesse.testsystems.slim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomComparatorRegistry {

  final Map<String, CustomComparator> customComparators = new HashMap<>();

  public CustomComparator getCustomComparatorForPrefix(String prefix) {
    if (customComparators.containsKey(prefix))
      return customComparators.get(prefix);
    else
      return null;
  }

  public void addCustomComparator(String prefix, CustomComparator customComparator) {
    customComparators.put(prefix, customComparator);
  }

  public Map<String, CustomComparator> getCustomComparators() {
    return Collections.unmodifiableMap(customComparators);
  }

}
