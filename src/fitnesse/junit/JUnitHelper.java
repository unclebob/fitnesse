package fitnesse.junit;


import junit.framework.Assert;

import org.junit.Test;

import fitnesse.Arguments;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesseMain.FitNesseMain;

public class JUnitHelper {
  
  private final String fitNesseRootPath; //= "/home/goyqo/work/fitnesse";
  private final String outputPath; //= "/tmp/fitnesse";
  private final ResultsListener resultsListener;
  
  public JUnitHelper(String fitNesseRootPath, String outputPath) {
    this(fitNesseRootPath,outputPath,new PrintTestListener());
  }
  
  public JUnitHelper(String fitNesseDir, String outputDir,
      ResultsListener resultsListener) {
        fitNesseRootPath = fitNesseDir;
        outputPath = outputDir;
        this.resultsListener = resultsListener;
  }

  public void assertTestPasses(String testName) throws Exception{
    assertPasses(testName, "test");
  }
  public void assertSuitePasses(String suiteName) throws Exception{
    assertPasses(suiteName, "suite");
    FitNesseContext ctx;
  }
  public void assertPasses(String pageName, String pageType) throws Exception{
    System.out.println("fitNessePath="+fitNesseRootPath);
    System.out.println("outputPath="+outputPath);
    JavaFormatter testFormatter=JavaFormatter.getInstance(pageName);
    testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputPath,fitNesseRootPath));
    testFormatter.setListener(resultsListener);
    Arguments arguments=new Arguments();
    arguments.setDaysTillVersionsExpire("0");
    arguments.setInstallOnly(false);
    arguments.setOmitUpdates(true);
    arguments.setRootPath(fitNesseRootPath);
    arguments.setCommand(pageName+"?"+pageType+"&format=java"); 
    FitNesseMain.dontExitAfterSingleCommand=true;
    FitNesseMain.launchFitNesse(arguments);   
    Assert.assertEquals("wrong", 0, testFormatter.getTotalSummary().wrong);
    Assert.assertEquals("exceptions", 0, testFormatter.getTotalSummary().exceptions);
  }
}
