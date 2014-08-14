package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FitNesseSuite.class)
@FitNesseSuite.Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitNesseSuite.FitnesseDir(".")
@FitNesseSuite.OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@FitNesseSuite.DebugMode(false)
public class FitNesseSuiteExampleTestNoDebug {

  @Test
  public void dummy(){
    
  }
}
