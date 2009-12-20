package fitnesse.junit;

import org.junit.Test;

import fitnesse.Arguments;
import fitnesse.responders.run.JavaFormatter;
import fitnesseMain.FitNesseMain;

public class JUnitTestHelper {
  
  private static final String fitNesseRootPath = "/home/goyqo/work/fitnesse";
  private static final String outputPath = "/tmp/fitnesse";
  
  @Test
  public void checkSuite() throws Exception{
    assertPasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "suite");
  }
  
  public void assertTestPasses(String testName) throws Exception{
    assertPasses(testName, "test");
  }
  public void assertSuitePasses(String suiteName) throws Exception{
    assertPasses(suiteName, "suite");
  }
  public void assertPasses(String suiteName, String pageType) throws Exception{
    JavaFormatter.getInstance().setResultsRepository(new JavaFormatter.FolderResultsRepository(outputPath,fitNesseRootPath));
    Arguments arguments=new Arguments();
    arguments.setDaysTillVersionsExpire("0");
    arguments.setInstallOnly(false);
    arguments.setOmitUpdates(true);
    arguments.setRootPath(fitNesseRootPath);
    arguments.setCommand(suiteName+"?"+pageType+"&format=java"); 
    FitNesseMain.dontExitAfterSingleCommand=true;
    FitNesseMain.launchFitNesse(arguments);   
    if ("suite".equals(pageType)){
      JavaFormatter.getInstance().writeSummary(suiteName);
    }
  }
}
