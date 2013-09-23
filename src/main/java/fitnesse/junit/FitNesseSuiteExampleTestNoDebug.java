package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseSuite.DebugMode;
import fitnesse.junit.FitNesseSuite.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseSuite.OutputDir;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitnesseDir(".")
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@DebugMode(false)
public class FitNesseSuiteExampleTestNoDebug {

  @Test
  public void dummy(){
    
  }
}
