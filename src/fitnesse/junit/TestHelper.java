package fitnesse.junit;

import fitnesseMain.Arguments;
import fitnesse.reporting.JavaFormatter;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesseMain.FitNesseMain;

public class TestHelper {
  
  private final String fitNesseRootPath;
  private final String outputPath;
  private final TestSystemListener resultListener;
  private boolean debug = true;
  
  public static final String PAGE_TYPE_SUITE="suite";
  public static final String PAGE_TYPE_TEST="test";
  
  public TestHelper(String fitNesseRootPath, String outputPath){
    this(fitNesseRootPath, outputPath, new PrintTestListener());
  }
  public TestHelper(String fitNesseRootPath, String outputPath, TestSystemListener listener) {
    this.fitNesseRootPath = fitNesseRootPath;
    this.outputPath = outputPath;
    this.resultListener = listener;
  }
  public TestSummary runSuite(String suiteName) throws Exception{
    return run(suiteName, PAGE_TYPE_SUITE);
  }
  public TestSummary runSuite(String suiteName, String suiteFilter) throws Exception{
    return run(suiteName, PAGE_TYPE_SUITE, suiteFilter);
  }
  public TestSummary runTest(String suiteName) throws Exception{
    return run(suiteName, PAGE_TYPE_TEST);
  }
  public  TestSummary run(String pageName, String pageType) throws Exception{
    return run(pageName, pageType, null);
  }
  public  TestSummary run(String pageName, String pageType, String suiteFilter, int port) throws Exception{
    return run(pageName, pageType, suiteFilter, null, port);
  }
  public  TestSummary run(String pageName, String pageType, String suiteFilter, String excludeSuiteFilter, int port) throws Exception{
    JavaFormatter testFormatter=JavaFormatter.getInstance(pageName);
    testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputPath));
    testFormatter.setListener(resultListener);
    Arguments arguments=new Arguments("-e", "0",
            "-o",
            "-p", String.valueOf(port),
            "-d", fitNesseRootPath,
            "-c", new CommandBuilder(pageName, pageType).withSuiteFilter(suiteFilter).withExcludeSuiteFilter(excludeSuiteFilter).withDebug(debug).build());
    new FitNesseMain().launchFitNesse(arguments);
    return testFormatter.getTotalSummary();
  }
  public  TestSummary run(String pageName, String pageType, String suiteFilter) throws Exception{
    return run(pageName, pageType, suiteFilter, 0);
  }
  
  public void setDebugMode(boolean enabled) {
    debug = enabled;
  }
 

}
