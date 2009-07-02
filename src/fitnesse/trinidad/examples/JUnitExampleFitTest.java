/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad.examples;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import fitnesse.trinidad.*;

public class JUnitExampleFitTest {
  JUnitHelper helper;

  @Before
  public void initHelper() throws Exception {
    helper = new JUnitHelper(new TestRunner(new FitNesseRepository("."),
        new FitTestEngine(), new File(System.getProperty("java.io.tmpdir"),
            "fitnesse").getAbsolutePath()));
  }

  @Test
  public void runSuite() throws Exception {
    helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteFixtureTests");
  }

  @Test
  public void runSingleTest() throws Exception {
    helper
        .assertTestPasses("FitNesse.SuiteAcceptanceTests.SuiteFixtureTests.SuiteRowFixtureSpec.TestBasicRowFixture");
  }
}
