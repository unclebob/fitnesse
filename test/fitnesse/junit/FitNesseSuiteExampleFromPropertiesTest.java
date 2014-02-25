package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseSuite.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseSuite.OutputDir;
import fitnesse.junit.FitNesseSuite.ConfigFile;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitnesseDir(systemProperty = "fitnesse.root.dir.parent")
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@ConfigFile("plugins.properties")
public class FitNesseSuiteExampleFromPropertiesTest {

  @Test
  public void dummy(){
    
  }
}
