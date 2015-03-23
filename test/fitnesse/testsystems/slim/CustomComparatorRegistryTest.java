package fitnesse.testsystems.slim;

import org.junit.Assert;

import org.junit.Test;

public class CustomComparatorRegistryTest {

  @Test
  public void useConverterFromCustomizing() {
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();
    customComparatorRegistry.addCustomComparator("prefix", new Comparator());

    CustomComparator comparator = customComparatorRegistry.getCustomComparatorForPrefix("prefix");
    Assert.assertTrue(comparator.matches("SAME", "same"));
  }

  static class Comparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
      return expected.equalsIgnoreCase(actual);
    }
  }
}
