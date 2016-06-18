package fitnesse.junit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseRunner.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseRunner.OutputDir;
import fitnesse.junit.FitNesseRunner.ConfigFile;


@Ignore
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
