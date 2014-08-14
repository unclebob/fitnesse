package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FitNesseSuite.class)
@FitNesseSuite.Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitNesseSuite.FitnesseDir(".")
@FitNesseSuite.OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@FitNesseSuite.SuiteFilter("testSuite")
@FitNesseSuite.ExcludeSuiteFilter("excludedSuite")
@FitNesseSuite.DebugMode(true)
public class FitNesseSuiteWithFilterExampleTest {
  @Test
  public void dummy(){
    
  }
}
