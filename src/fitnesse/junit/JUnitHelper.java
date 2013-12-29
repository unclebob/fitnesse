package fitnesse.junit;


import fitnesse.reporting.JavaFormatter;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;

import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JUnitHelper {

  public static final String PAGE_TYPE_SUITE="suite";
  public static final String PAGE_TYPE_TEST="test";
  private final String fitNesseRootPath;
  private final String outputPath;
  private final TestSystemListener resultListener;
  private boolean debug = true;
  private int port = 0;

  public JUnitHelper(String fitNesseRootPath, String outputPath) {
    this(fitNesseRootPath, outputPath, new PrintTestListener());
  }

  public JUnitHelper(String fitNesseRootPath, String outputPath,
                     TestSystemListener listener) {
    this.fitNesseRootPath = fitNesseRootPath;
    this.outputPath = outputPath;
    this.resultListener = listener;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setDebugMode(boolean enabled) {
    debug = enabled;
  }

  public void assertTestPasses(String testName) throws Exception {
    assertPasses(testName, PAGE_TYPE_TEST, null);
  }

  public void assertSuitePasses(String suiteName) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, null);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, suiteFilter);
  }

  public void assertSuitePasses(String suiteName, String suiteFilter, String excludeSuiteFilter) throws Exception {
    assertPasses(suiteName, PAGE_TYPE_SUITE, suiteFilter, excludeSuiteFilter);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception {
    assertPasses(pageName, pageType, suiteFilter, null);
  }

  public void assertPasses(String pageName, String pageType, String suiteFilter, String excludeSuiteFilter) throws Exception {
    TestSummary summary = run(pageName, pageType, suiteFilter, excludeSuiteFilter);
    assertEquals("wrong", 0, summary.wrong);
    assertEquals("exceptions", 0, summary.exceptions);
    assertTrue(msgAtLeastOneTest(pageName, summary), summary.right > 0);
  }

  private String msgAtLeastOneTest(String pageName, TestSummary summary) {
    return MessageFormat.format("at least one test executed in {0}\n{1}",
              pageName, summary.toString());
  }

  public  TestSummary run(String pageName, String pageType, String suiteFilter, String excludeSuiteFilter) throws Exception{
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
}
