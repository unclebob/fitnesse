/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.IOException;

import org.junit.Assert;

import fit.Counts;

public class JUnitHelper {
  private TestRunner trinidad;

  public JUnitHelper(TestRepository repository, TestEngine testEngine,
      String output) throws IOException {
    this(new TestRunner(repository, testEngine, output));
  }

  public JUnitHelper(TestRunner testRunner) throws IOException {
    this.trinidad = testRunner;
  }

  public void assertTestPasses(String testName) throws Exception {
    Counts ct = trinidad.runTest(testName);
    Assert.assertEquals("exceptions in tests", 0, ct.exceptions);
    Assert.assertEquals("wrong tests", 0, ct.wrong);
  }

  public void assertSuitePasses(String suiteName) throws Exception {
    Counts ct = trinidad.runSuite(suiteName);
    Assert.assertEquals("exceptions in tests", 0, ct.exceptions);
    Assert.assertEquals("wrong tests", 0, ct.wrong);
  }

}
