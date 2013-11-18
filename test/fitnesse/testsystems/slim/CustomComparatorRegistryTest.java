package fitnesse.testsystems.slim;

import junit.framework.Assert;

import org.junit.Test;

public class CustomComparatorRegistryTest {

  @Test
  public void useConverterFromCustomizing() {
    CustomComparatorRegistry.addCustomComparator("prefix", new Comparator());

    CustomComparator comparator = CustomComparatorRegistry.getCustomComparatorForPrefix("prefix");
    Assert.assertTrue(comparator.matches("SAME", "same"));
  }

  static class Comparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
      return expected.equalsIgnoreCase(actual);
    }
  }
}
