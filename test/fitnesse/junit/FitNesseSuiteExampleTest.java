package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseRunner.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseRunner.OutputDir;
import fitnesse.junit.FitNesseRunner.ConfigFile;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableSuite")
@FitnesseDir(".")
@OutputDir("tmp")
@ConfigFile("plugins.properties")
public class FitNesseSuiteExampleTest {

  @Test
  public void dummy(){
    
  }
}
