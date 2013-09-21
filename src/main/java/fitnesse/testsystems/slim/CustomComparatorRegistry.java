package fitnesse.testsystems.slim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomComparatorRegistry {

  final static Map<String, CustomComparator> customComparators = new HashMap<String, CustomComparator>();

  public static  CustomComparator getCustomComparatorForPrefix(String prefix) {
    if (customComparators.containsKey(prefix))
      return (CustomComparator) customComparators.get(prefix);
    else
      return null;
  }

  public static void addCustomComparator(String prefix, CustomComparator customComparator) {
    customComparators.put(prefix, customComparator);
  }

  public static Map<String, CustomComparator> getCustomComparators() {
    return Collections.unmodifiableMap(customComparators);
  }

}
