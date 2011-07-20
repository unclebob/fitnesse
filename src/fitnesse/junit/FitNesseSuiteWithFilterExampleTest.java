package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseSuite.DebugMode;
import fitnesse.junit.FitNesseSuite.ExcludeSuiteFilter;
import fitnesse.junit.FitNesseSuite.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseSuite.OutputDir;
import fitnesse.junit.FitNesseSuite.SuiteFilter;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitnesseDir(".")
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@SuiteFilter("testSuite")
@ExcludeSuiteFilter("excludedSuite")
@DebugMode(true)
public class FitNesseSuiteWithFilterExampleTest {
  @Test
  public void dummy(){
    
  }
}
