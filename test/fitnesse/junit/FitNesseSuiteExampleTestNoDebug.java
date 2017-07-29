package fitnesse.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

import fitnesse.junit.FitNesseRunner.DebugMode;
import fitnesse.junit.FitNesseRunner.ExcludeSuiteFilter;
import fitnesse.junit.FitNesseRunner.FitnesseDir;
import fitnesse.junit.FitNesseSuite.Name;
import fitnesse.junit.FitNesseRunner.OutputDir;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitnesseDir(".")
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@DebugMode(false)
@ExcludeSuiteFilter("noJunit")
public class FitNesseSuiteExampleTestNoDebug {

  @Test
  public void dummy(){
    
  }
}
