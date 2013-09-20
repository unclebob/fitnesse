package fitnesse.testsystems.slim;

/**
 * Custom Comparator for matching strings (registered using the CustomComparator plugin).
 */
public interface CustomComparator {

  /**
   * Compare two string representations, to determine whether the expected result matches
   * the actual result.
   * @param actual String representation of the actual result
   * @param expected String representation of the expected result
   * @return true if they match, false if they don't match
   * @throws Throwable if an exception or error is thrown, the throwable message is appended
   * to the failure output
   */
  boolean matches(String actual, String expected);

}
