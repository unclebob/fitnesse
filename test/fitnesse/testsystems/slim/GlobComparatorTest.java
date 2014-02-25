package fitnesse.testsystems.slim;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GlobComparatorTest {

  @Test
  public void matchesGlobPatterns() {
    GlobComparator comparator = new GlobComparator();
    assertTrue(comparator.matches("foo", "fo*"));
    assertTrue(comparator.matches("bar", "b*"));
    assertTrue(comparator.matches("bar", "*"));

  }
}
