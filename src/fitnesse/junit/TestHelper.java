package fitnesse.junit;

import fitnesse.Arguments;
import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesseMain.FitNesseMain;

public class TestHelper {
  
  private final String fitNesseRootPath;
  private final String outputPath;
  private final ResultsListener resultListener;
  
  public static final String PAGE_TYPE_SUITE="suite";
  public static final String PAGE_TYPE_TEST="test";
  
  public TestHelper(String fitNesseRootPath, String outputPath){
    this(fitNesseRootPath, outputPath, new PrintTestListener());
  }
  public TestHelper(String fitNesseRootPath, String outputPath, ResultsListener resultListener) {
    this.fitNesseRootPath = fitNesseRootPath;
    this.outputPath = outputPath;
    this.resultListener = resultListener;
  }
  public TestSummary runSuite(String suiteName) throws Exception{
    return run(suiteName, PAGE_TYPE_SUITE);
  }
  public TestSummary runTest(String suiteName) throws Exception{
    return run(suiteName, PAGE_TYPE_TEST);
  }
  public  TestSummary run(String pageName, String pageType) throws Exception{
    JavaFormatter testFormatter=JavaFormatter.getInstance(pageName);
    testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputPath,fitNesseRootPath));
    testFormatter.setListener(resultListener);
    Arguments arguments=new Arguments();
    arguments.setDaysTillVersionsExpire("0");
    arguments.setInstallOnly(false);
    arguments.setOmitUpdates(true);
    arguments.setRootPath(fitNesseRootPath);
    arguments.setCommand(pageName+"?"+pageType+"&debug=true&format=java"); 
    FitNesseMain.dontExitAfterSingleCommand=true;
    FitNesseMain.launchFitNesse(arguments);   
    return testFormatter.getTotalSummary();
  }

}
