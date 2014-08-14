package fitnesse.junit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseSuite.*;


@Ignore
@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitNesseSuite.FitnesseDir(systemProperty = "fitnesse.root.dir.parent")
@FitNesseSuite.OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@FitNesseSuite.ConfigFile("plugins.properties")
public class FitNesseSuiteExampleFromPropertiesTest {

  @Test
  public void dummy(){
    
  }
}
