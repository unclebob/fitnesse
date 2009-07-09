/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */
package fitnesse.trinidad.examples;

import org.junit.runner.RunWith;
import org.junit.Test;

import fitnesse.trinidad.*;
import fitnesse.trinidad.FitnesseSuite.*;

@RunWith(FitnesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteFixtureTests")
@FitnesseDir(".")
// @TestEngine(FitTestEngine.class) //this is optional since it's the default
// @OutputDir("/tmp/fitnesse") //Specify an absolute or relative path
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
public class JUnitExampleFitnesseSuiteTest {
  @Test
  public void dummy() throws Exception {

  }

}
