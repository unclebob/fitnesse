/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

public interface TestEngine {
  public TestResult runTest(TestDescriptor test);
}
