package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(FitNesseSuite.class)
@FitNesseSuite.Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitNesseSuite.FitnesseDir(".")
@FitNesseSuite.OutputDir("tmp")
@FitNesseSuite.ConfigFile("plugins.properties")
public class FitNesseSuiteExampleTest {

  @Test
  public void dummy(){
    
  }
}
